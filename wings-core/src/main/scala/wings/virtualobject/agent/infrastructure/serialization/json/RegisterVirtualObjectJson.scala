package wings.virtualobject.agent.infrastructure.serialization.json

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._
import play.api.libs.json.{JsObject, Json, OFormat, OWrites, Reads, _}
import wings.virtualobject.agent.domain.messages.command._
import wings.virtualobject.agent.infrastructure.keys.RegisterVirtualObjectKeys


object RegisterVirtualObjectJson {

  val NameAcquisitionRequestReads: Reads[NameAcquisitionRequest] = (
    (__ \ RegisterVirtualObjectKeys.OperationKey).read[String].filter(_ == RegisterVirtualObjectKeys.NameAcquisitionRequestPath) ~>
      (__ \ RegisterVirtualObjectKeys.VirtualObjectIdKey).read[UUID]).map { id =>
    NameAcquisitionRequest(id)
  }

  object NameAcquisitionRequestWrites extends OWrites[NameAcquisitionRequest] {

    override def writes(o: NameAcquisitionRequest): JsObject =
      Json.obj(
        RegisterVirtualObjectKeys.OperationKey -> RegisterVirtualObjectKeys.NameAcquisitionRequestPath,
        RegisterVirtualObjectKeys.VirtualObjectIdKey -> o.virtualObjectId
      )

  }

  val NameAcquisitionRequestFormat = OFormat(NameAcquisitionRequestReads, NameAcquisitionRequestWrites)

  val NameAcquisitionAckReads: Reads[NameAcquisitionAck] = (
    (__ \ RegisterVirtualObjectKeys.OperationKey).read[String].filter(_ == RegisterVirtualObjectKeys.NameAcquisitionAckPath) ~>
      (__ \ RegisterVirtualObjectKeys.VirtualObjectIdKey).read[UUID]).map { id =>
    NameAcquisitionAck(id)
  }

  object NameAcquisitionAckWrites extends OWrites[NameAcquisitionAck] {

    override def writes(o: NameAcquisitionAck): JsObject =
      Json.obj(
        RegisterVirtualObjectKeys.OperationKey -> RegisterVirtualObjectKeys.NameAcquisitionAckPath,
        RegisterVirtualObjectKeys.VirtualObjectIdKey -> o.virtualObjectId
      )

  }

  val NameAcquisitionAckFormat = OFormat(NameAcquisitionAckReads, NameAcquisitionAckWrites)

  val NameAcquisitionRejectReads: Reads[NameAcquisitionReject] = (
    (__ \ RegisterVirtualObjectKeys.OperationKey).read[String].filter(_ == RegisterVirtualObjectKeys.NameAcquisitionRejectPath) ~>
      (__ \ RegisterVirtualObjectKeys.VirtualObjectIdKey).read[UUID]).map { id =>
    NameAcquisitionReject(id)
  }

  object NameAcquisitionRejectWrites extends OWrites[NameAcquisitionReject] {

    override def writes(o: NameAcquisitionReject): JsObject =
      Json.obj(
        RegisterVirtualObjectKeys.OperationKey -> RegisterVirtualObjectKeys.NameAcquisitionRejectPath,
        RegisterVirtualObjectKeys.VirtualObjectIdKey -> o.virtualObjectId
      )

  }

  val NameAcquisitionRejectFormat = OFormat(NameAcquisitionRejectReads, NameAcquisitionRejectWrites)


  object RegisterVirtualObjectReads extends Reads[RegisterVirtualObject] {

    override def reads(json: JsValue): JsResult[RegisterVirtualObject] =
      json.validate[NameAcquisitionRequest](NameAcquisitionRequestReads) or
        json.validate[NameAcquisitionAck](NameAcquisitionAckReads) or
        json.validate[NameAcquisitionReject](NameAcquisitionRejectReads)

  }

}
