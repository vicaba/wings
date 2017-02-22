package wings.virtualobjectagent.infrastructure.messages.serialization.json

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsObject, Json, OFormat, OWrites, Reads, _}
import play.api.libs.json.Writes._

import wings.virtualobjectagent.domain.messages.command._
import wings.virtualobjectagent.infrastructure.messages.keys.RegisterVirtualObjectIdKeys

object RegisterVirtualObjectIdJson {

  val NameAcquisitionRequestReads: Reads[NameAcquisitionRequest] = ((__ \ RegisterVirtualObjectIdKeys.OperationKey)
    .read[String]
    .filter(_ == RegisterVirtualObjectIdKeys.NameAcquisitionRequestPath) ~>
    (__ \ RegisterVirtualObjectIdKeys.VirtualObjectIdKey).read[UUID]).map { id =>
    NameAcquisitionRequest(id)
  }

  object NameAcquisitionRequestWrites extends OWrites[NameAcquisitionRequest] {

    override def writes(o: NameAcquisitionRequest): JsObject =
      Json.obj(
        RegisterVirtualObjectIdKeys.OperationKey       -> RegisterVirtualObjectIdKeys.NameAcquisitionRequestPath,
        RegisterVirtualObjectIdKeys.VirtualObjectIdKey -> o.virtualObjectId
      )

  }

  val NameAcquisitionRequestFormat = OFormat(NameAcquisitionRequestReads, NameAcquisitionRequestWrites)

  val NameAcquisitionAckReads: Reads[NameAcquisitionAck] = ((__ \ RegisterVirtualObjectIdKeys.OperationKey)
    .read[String]
    .filter(_ == RegisterVirtualObjectIdKeys.NameAcquisitionAckPath) ~>
    (__ \ RegisterVirtualObjectIdKeys.VirtualObjectIdKey).read[UUID]).map { id =>
    NameAcquisitionAck(id)
  }

  object NameAcquisitionAckWrites extends OWrites[NameAcquisitionAck] {

    override def writes(o: NameAcquisitionAck): JsObject =
      Json.obj(
        RegisterVirtualObjectIdKeys.OperationKey       -> RegisterVirtualObjectIdKeys.NameAcquisitionAckPath,
        RegisterVirtualObjectIdKeys.VirtualObjectIdKey -> o.virtualObjectId
      )

  }

  val NameAcquisitionAckFormat = OFormat(NameAcquisitionAckReads, NameAcquisitionAckWrites)

  val NameAcquisitionRejectReads: Reads[NameAcquisitionReject] = ((__ \ RegisterVirtualObjectIdKeys.OperationKey)
    .read[String]
    .filter(_ == RegisterVirtualObjectIdKeys.NameAcquisitionRejectPath) ~>
    (__ \ RegisterVirtualObjectIdKeys.VirtualObjectIdKey).read[UUID]).map { id =>
    NameAcquisitionReject(id)
  }

  object NameAcquisitionRejectWrites extends OWrites[NameAcquisitionReject] {

    override def writes(o: NameAcquisitionReject): JsObject =
      Json.obj(
        RegisterVirtualObjectIdKeys.OperationKey       -> RegisterVirtualObjectIdKeys.NameAcquisitionRejectPath,
        RegisterVirtualObjectIdKeys.VirtualObjectIdKey -> o.virtualObjectId
      )

  }

  val NameAcquisitionRejectFormat = OFormat(NameAcquisitionRejectReads, NameAcquisitionRejectWrites)

  object RegisterVirtualObjectIdReads extends Reads[RegisterVirtualObjectId] {

    override def reads(json: JsValue): JsResult[RegisterVirtualObjectId] =
      json.validate[NameAcquisitionRequest](NameAcquisitionRequestReads) or
        json.validate[NameAcquisitionAck](NameAcquisitionAckReads) or
        json.validate[NameAcquisitionReject](NameAcquisitionRejectReads)

  }

}
