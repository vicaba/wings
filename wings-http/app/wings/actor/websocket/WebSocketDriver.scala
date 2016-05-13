package wings.actor.websocket

import java.util.UUID

import akka.actor.{Actor, Props, ActorRef}
import play.api.libs.json.{JsResult, JsValue, Json}
import wings.actor.pipeline.MsgEnv
import wings.agent.DeviceDriver
import wings.m2m.VOMessage
import wings.m2m.conf.model.Config
import wings.model.virtual.operations.{VoActuate, VoWatch}
import wings.model.virtual.virtualobject.actuated.ActuatedValue
import wings.model.virtual.virtualobject.sensed.SensedValue

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
    case voActuate: VoActuate =>
      val json = Json.toJson(voActuate).toString()
      out ! json.toString
    case sv: SensedValue =>
      val json = Json.toJson(sv).toString()
      out ! json.toString
    case voActuate: VoActuate =>
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
    json.validate[Config] orElse json.validate[VOMessage] orElse
      json.validate[VoWatch] orElse json.validate[SensedValue] orElse
      json.validate[VoActuate]
  }

}
