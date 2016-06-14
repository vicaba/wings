package controllers.admin

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import com.google.inject.Inject
import common.JsonTemplates
import common.request.{AuthenticatedAction, CanBeAuthenticatedAction}
import models.user.UserIdentityManager
import models.user.services.db.mongo.UserMongoService
import play.api.libs.json.Json
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Controller, WebSocket}
import scaldi.Injectable._
import websocket.WebSocketHandler
import wings.actor.websocket.WebSocketActor
import wings.config.DependencyInjector._
import wings.model.virtual.virtualobject.services.db.mongo.VirtualObjectMongoService
import wings.model.virtual.virtualobject.{VO, VOIdentityManager}
import wings.services.db.MongoEnvironment

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class WebSocket @Inject() (implicit system: ActorSystem, materializer: Materializer) extends Controller {

  val mongoEnvironment: MongoEnvironment = inject[MongoEnvironment](identified by 'MongoEnvironment)

  def createAPI = CanBeAuthenticatedAction.async(parse.json) {
    request =>
      CanBeAuthenticatedAction.Fold(request) {
        implicit ar =>

          val userService = new UserMongoService(mongoEnvironment.mainDb)(UserIdentityManager)
          userService.findOneByCriteria(Json.obj(UserIdentityManager.name -> request.session.get(UserIdentityManager.name))).flatMap {
            case Some(user) =>
              val virtualObjectService = new VirtualObjectMongoService(mongoEnvironment.mainDb)(VOIdentityManager)
              val voId = VOIdentityManager.next
              val voIdOpt = Some(voId)
              if (user.virtualObjectId.isEmpty) {

                val thisVo = VO(
                  Some(UUID.randomUUID()), voId, None, None, None, s"web/${user.username}",
                  None, ZonedDateTime.now(), None, None, None)

                virtualObjectService.create(thisVo).flatMap {
                  case Left(wr) => Future {
                    BadRequest(JsonTemplates.singleMsg(wr.message))
                  }
                  case Right(vo) =>
                    userService.update(user.copy(virtualObjectId = voIdOpt)).map {
                      case Left(wr) => BadRequest(JsonTemplates.singleMsg(wr.message))
                      case Right(u) => Created("")
                    }
                }
              } else {
                Future {
                  Conflict(JsonTemplates.singleMsg("WebSocket resource already exists"))
                }
              }
            case None => Future {
              BadRequest(JsonTemplates.singleMsg("The current user cannot be found. It may have been deleted during this request"))
            }
          }
      } {
        implicit ur => Future {
          Unauthorized("")
        }
      }
  }

  def socketAPI = WebSocket.acceptOrResult[String, String] { request =>

      AuthenticatedAction.sessionAuthenticate(request) match {

        case Some((name, uuid)) =>
          val userService = new UserMongoService(mongoEnvironment.mainDb)(UserIdentityManager)

          userService.findOneByCriteria(Json.obj(UserIdentityManager.name -> uuid)).map {
            case Some(user) =>
              val coreAgentProps = (voId: UUID, out: ActorRef) => WebSocketActor.props(voId, user)(out)
              Right(ActorFlow.actorRef(out => WebSocketHandler.props(coreAgentProps, out)))
            case _ => Left(Unauthorized(""))

          }
        case _ => Future {
          Left(Unauthorized(""))
        }
      }
  }


}
