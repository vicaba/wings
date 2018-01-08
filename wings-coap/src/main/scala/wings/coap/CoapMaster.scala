package wings.coap

import java.util.UUID

import akka.actor.{Actor, Props}
import org.eclipse.californium.core.coap.CoAP.{Code, ResponseCode}
import org.eclipse.californium.core.server.resources.CoapExchange
import play.api.libs.json.{JsValue, Json}
import wings.virtualobjectagent.domain.messages.command.{NameAcquisitionAck, NameAcquisitionRequest, RegisterVirtualObjectId}
import wings.virtualobjectagent.infrastructure.messages.serialization.json.Implicits._

import scala.util.Try

object CoapMaster {
  def props(): Props = Props(CoapMaster())
}


case class CoapMaster() extends Actor
{

  type ProtocolResponse = () => Unit

  override def receive: Receive =
  {
    case message: CoapExchange =>
      message.getRequestCode match
      {
        case Code.GET => onGet(message)()
        case Code.PUT => onPut(message)()
      }
  }

  def onGet(message: CoapExchange): ProtocolResponse =
    () => message.respond(Json.toJson(provideIdentity).toString())

  def onPut(message: CoapExchange): ProtocolResponse =
    Try(Json.parse(message.getRequestText)).map(_.validate[RegisterVirtualObjectId]).map
    {

      case nameAcquisitionRequest: NameAcquisitionRequest =>
        () => message.respond(Json.toJson(candidateIdentityReceived(nameAcquisitionRequest)).toString())
      case _ => () => message.respond(ResponseCode.NOT_ACCEPTABLE)

    }.getOrElse(() => message.respond(ResponseCode.BAD_REQUEST))

  def candidateIdentityReceived(nameAcquisitionRequest: NameAcquisitionRequest): RegisterVirtualObjectId =
    NameAcquisitionAck(nameAcquisitionRequest.virtualObjectId)

  def provideIdentity: RegisterVirtualObjectId = NameAcquisitionAck(UUID.randomUUID())

}
