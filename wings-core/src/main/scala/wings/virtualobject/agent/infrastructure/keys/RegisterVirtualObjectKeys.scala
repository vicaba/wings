package wings.virtualobject.agent.infrastructure.keys

import wings.virtualobject.infrastructure.keys.VirtualObjectKeys

object RegisterVirtualObjectKeys {

  val OperationKey = "op"

  val NameAcquisitionRequestPath = "vo/register/name/request"

  val NameAcquisitionAckPath = "vo/register/path/ack"

  val NameAcquisitionRejectPath = "vo/register/path/reject"

  val VirtualObjectIdKey = VirtualObjectKeys.IdKey

}
