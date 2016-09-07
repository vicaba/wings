package controllers.admin

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import com.google.inject.Inject
import common.request.AuthenticatedAction
import models.user.UserIdentityManager
import models.user.services.db.mongo.UserMongoService
import play.api.libs.json.Json
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Controller, WebSocket}
import scaldi.Injectable._
import websocket.WebSocketHandler
import wings.actor.websocket.WebSocketActor
import wings.config.DependencyInjector._
import wings.services.db.MongoEnvironment
import wings.virtualobject.infrastructure.serialization.json.Implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class WebSocket @Inject() (implicit system: ActorSystem, materializer: Materializer) extends Controller {

  val mongoEnvironment: MongoEnvironment = inject[MongoEnvironment](identified by 'MongoEnvironment)

  def socketAPI = WebSocket.acceptOrResult[String, String] { request =>

      AuthenticatedAction.sessionAuthenticate(request) match {

        case Some((name, uuid)) =>
          val userService = new UserMongoService(mongoEnvironment.mainDb)(UserIdentityManager)

          userService.findOneByCriteria(Json.obj(UserIdentityManager.name -> uuid)).map {
            case Some(user) =>
              val coreAgentProps = (voId: UUID, out: ActorRef) => WebSocketActor.props(voId, user)(out)
              Right(ActorFlow.actorRef(out => WebSocketHandler.props(coreAgentProps, out), Int.MaxValue))
            case _ => Left(Unauthorized(""))

          }
        case _ => Future {
          Left(Unauthorized(""))
        }
      }
  }


}
