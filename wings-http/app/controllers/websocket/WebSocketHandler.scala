package websocket

import java.util.UUID

import akka.actor._
import akka.event.Logging
import play.api.libs.json.{JsValue, Json}
import wings.virtualobjectagent.infrastructure.messages.serialization.json.Implicits._
import wings.virtualobjectagent.domain.agent.CoreAgent
import wings.virtualobjectagent.domain.agent.CoreAgentMessages.ToDeviceActor
import wings.virtualobjectagent.domain.messages.command.{NameAcquisitionAck, NameAcquisitionRequest, RegisterVirtualObjectId}

import scala.concurrent.duration._
import scala.util.Try

object WebSocketHandler {
  def props(agentProps: (UUID, ActorRef) => Props, webSocketOutputHandler: ActorRef) = Props(WebSocketHandler(agentProps, webSocketOutputHandler))
}

case class WebSocketHandler(agentProps: (UUID, ActorRef) => Props, webSocketOutputHandler: ActorRef)
  extends Actor with Stash {

  import context._

  val logger = Logging(context.system, this)


  override def preStart() = {
    logger.debug("WebSocketHandler Deployed")
    become(onStartReceive)
  }

  override def receive: Receive = onStartReceive

  def onStartReceive: Receive = {
    case msg: String =>
      logger.debug("onStartReceive: {}", msg)
      msgToJson(msg).map(validateJson(_).map {
        case cnf: NameAcquisitionRequest =>
          // TODO: Perform name resolution here
          webSocketOutputHandler ! Json.toJson(NameAcquisitionAck(cnf.virtualObjectId)).toString() // Send an ACK temporarily
          // TODO: Save Actor UUID to user
          val coreAgent = context.actorOf(agentProps(cnf.virtualObjectId, webSocketOutputHandler), CoreAgent.name)
          val tickToQueryForToDeviceActor = context.system.scheduler.schedule(0.millis, 500.millis, coreAgent, ToDeviceActor)
          become(waitForFirstToDeviceMessage(tickToQueryForToDeviceActor))
      })
    case _ => stash()
  }

  def becomeBridge(toDevice: ActorRef) = become(bridgeReceive(toDevice))

  def waitForFirstToDeviceMessage(tickToQueryForToDeviceActor: Cancellable): Receive = {
    case toDeviceActor: ActorRef =>
      tickToQueryForToDeviceActor.cancel()
      unstashAll()
      becomeBridge(toDeviceActor)
    case anyMsg: Any => stash(); logger.debug("Stashing Message: {}", anyMsg)

  }

  def bridgeReceive(toDevice: ActorRef): Receive = {
    case msg: String => toDevice ! msg; logger.debug("Forwarding message to Device: {}", msg)
    case _ => logger.debug("WebSocket Handler received an unknown message")
  }

  def msgToJson(s: String): Try[JsValue] = Try(Json.parse(s))

  def validateJson(json: JsValue) = {
    json.validate[RegisterVirtualObjectId]
  }

  override def postStop = {
    println("Actor Stopped")
  }

}
