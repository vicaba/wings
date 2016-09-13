package wings.actor.websocket

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import play.api.libs.json.{JsValue, Json}
import wings.m2m.VOMessage
import wings.virtualobject.agent.infrastructure.serialization.json.Implicits._
import wings.virtualobject.agent.domain.messages.command.{ActuateOnVirtualObject, RegisterVirtualObject, WatchVirtualObject}
import wings.virtualobject.agent.domain.messages.event.VirtualObjectSensed
import wings.virtualobject.agent.domain.{DeviceDriver, MsgEnv}
import wings.virtualobject.agent.infrastructure.serialization.json.Implicits._

import scala.util.Try

object WebSocketDriver {
  def props(virtualObjectId: UUID, out: ActorRef, continuation: ActorRef) = Props(WebSocketDriver(virtualObjectId, out, continuation))
}

case class WebSocketDriver(virtualObjectId: UUID, out: ActorRef, continuation: ActorRef) extends Actor with DeviceDriver {

  import context._

  override type DeviceMessageType = String
  override type DeviceConnectionContext = ActorRef

  override def preStart() = {
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

  override def toArchitectureReceive(dc: DeviceConnectionContext, continuation: ActorRef): PartialFunction[DeviceMessageType, Unit] = {
    case msg: String =>
      logger.debug(s"Received $msg")
      msgToJson(msg).map(validateJson(_).map{continuation ! MsgEnv.ToArch(_)}).recover {
        case t: Throwable => t.printStackTrace()
      }
  }

  def msgToJson(s: String): Try[JsValue] = Try(Json.parse(s))

  def validateJson(json: JsValue) = {
    json.validate[RegisterVirtualObject] orElse
      json.validate[VOMessage] orElse
      json.validate[WatchVirtualObject] orElse
      json.validate[VirtualObjectSensed] orElse
      json.validate[ActuateOnVirtualObject]
  }

}
