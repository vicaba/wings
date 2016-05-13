package controllers.admin

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.ActorRef
import akka.stream.Materializer
import com.google.inject.Inject
import common.JsonTemplates
import common.request.{AuthenticatedAction, CanBeAuthenticatedAction}
import play.api.Play.current
import database.mongodb.MongoEnvironment
import models.user.UserIdentityManager
import models.user.services.db.mongo.UserMongoService
import play.api.libs.json.Json
import play.api.mvc.{Controller, WebSocket}
import websocket.WebSocketHandler
import wings.actor.websocket.WebSocketActor
import wings.model.virtual.virtualobject.{VO, VOIdentityManager}
import wings.model.virtual.virtualobject.services.db.mongo.VirtualObjectMongoService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class WebSocket @Inject() (implicit val mat: Materializer) extends Controller {

  def createAPI = CanBeAuthenticatedAction.async(parse.json) {
    request =>
      CanBeAuthenticatedAction.Fold(request) {
        implicit ar =>

          val userService = new UserMongoService(MongoEnvironment.db1)(UserIdentityManager)
          userService.findOneByCriteria(Json.obj(UserIdentityManager.name -> request.session.get(UserIdentityManager.name))).flatMap {
            case Some(user) =>
              val virtualObjectService = new VirtualObjectMongoService(MongoEnvironment.db1)(VOIdentityManager)
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

  def connectAPI = WebSocket.tryAcceptWithActor[String, String] {
    request =>

      AuthenticatedAction.sessionAuthenticate(request) match {

        case Some((name, uuid)) =>
          val userService = new UserMongoService(MongoEnvironment.db1)(UserIdentityManager)

          userService.findOneByCriteria(Json.obj(UserIdentityManager.name -> uuid)).flatMap {
            case Some(user) =>
              println(Json.obj(VO.VOIDKey -> user.virtualObjectId).toString())

              VirtualObjectMongoService(MongoEnvironment.db1)(VOIdentityManager).findOneByCriteria(Json.obj(VO.VOIDKey -> user.virtualObjectId)).map {
                case Some(vo) => println("WebSocket resource found"); Right(WebSocketActor.props(vo.voId, user) _)
                case _ => Left(Forbidden)
              }
          }
        case _ => Future {
          Left(Unauthorized(""))
        }
      }
  }


  def socketAPI = WebSocket.tryAcceptWithActor[String, String] {
    request =>
    
    println("I'm here")

      AuthenticatedAction.sessionAuthenticate(request) match {

        case Some((name, uuid)) =>
          val userService = new UserMongoService(MongoEnvironment.db1)(UserIdentityManager)

          userService.findOneByCriteria(Json.obj(UserIdentityManager.name -> uuid)).map {
            case Some(user) =>
              println(Json.obj(VO.VOIDKey -> user.virtualObjectId).toString())
              val coreAgentProps = (voId: UUID, out: ActorRef) => WebSocketActor.props(voId, user)(out)
              Right(out => WebSocketHandler.props(coreAgentProps, out))
            case _ => Left(Unauthorized(""))

          }
        case _ => Future {
          Left(Unauthorized(""))
        }
      }
  }


}
