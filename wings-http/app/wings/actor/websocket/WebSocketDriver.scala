package wings.actor.websocket

import java.util.UUID

import scala.util.Try

import akka.actor.{Actor, ActorRef, Props}

import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, JsResult, JsValue}

import wings.virtualobjectagent.domain.agent.{DeviceDriver, MsgEnv}
import wings.virtualobjectagent.domain.messages.command.{ActuateOnVirtualObject, RegisterVirtualObjectId, VirtualObjectBasicDefinition, WatchVirtualObject}
import wings.virtualobjectagent.domain.messages.event.VirtualObjectSensed
import wings.virtualobjectagent.infrastructure.messages.serialization.json.Implicits._


object WebSocketDriver {
  def props(virtualObjectId: UUID, out: ActorRef, continuation: ActorRef): Props =
    Props(WebSocketDriver(virtualObjectId, out, continuation))
}

case class WebSocketDriver(virtualObjectId: UUID, out: ActorRef, continuation: ActorRef)
  extends Actor
    with DeviceDriver {

  import context._

  override type DeviceMessageType = String
  override type DeviceConnectionContext = ActorRef

  override def preStart(): Unit = {
    become(driverState(out, continuation))
  }

  override def receive: Actor.Receive = {
    case a: Any => println(a)
  }

  override def toDeviceReceive(dc: DeviceConnectionContext): PartialFunction[Any, Unit] = {
    case voActuate: ActuateOnVirtualObject =>
      val json = Json.toJson(voActuate).toString()
      out ! json.toString
    case sv: VirtualObjectSensed =>
      val json = Json.toJson(sv).toString()
      out ! json.toString
    case voActuate: ActuateOnVirtualObject =>
      val json = Json.toJson(voActuate)
      out ! json.toString
  }

  override def toArchitectureReceive(
                                      dc: DeviceConnectionContext,
                                      continuation: ActorRef
                                    ): PartialFunction[DeviceMessageType, Unit] = {
    case msg: String =>
      logger.debug(s"Received $msg")
      msgToJson(msg).map(validateJson(_).map {
        continuation ! MsgEnv.ToArch(_)
      }).recover {
        case t: Throwable => t.printStackTrace()
      }
  }

  def msgToJson(s: String): Try[JsValue] = Try(Json.parse(s))

  def validateJson(json: JsValue): JsResult[Object] = {
    json.validate[RegisterVirtualObjectId] or
      json.validate[VirtualObjectBasicDefinition] or
      json.validate[WatchVirtualObject] or
      json.validate[VirtualObjectSensed] or
      json.validate[ActuateOnVirtualObject]
  }

}
