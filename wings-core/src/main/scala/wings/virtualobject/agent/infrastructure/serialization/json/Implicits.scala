package wings.virtualobject.agent.infrastructure.serialization.json

import wings.virtualobject.agent.infrastructure.event.serialization.json.VirtualObjectOperatedJson

object Implicits {

  lazy implicit val ActuateOnVirtualObjectJsonSerializer = OperateOnVirtualObjectJson.ActuateOnVirtualObjectFormat

  lazy implicit val WatchVirtualObjectJsonSerializer = OperateOnVirtualObjectJson.WatchVirtualObjectFormat

  lazy implicit val VirtualObjectActuatedJsonSerializer = VirtualObjectOperatedJson.VirtualObjectActuatedFormat

  lazy implicit val VirtualObjectSensedJsonSerializer = VirtualObjectOperatedJson.VirtualObjectSensedFormat
  
  lazy implicit val NameAcquisitionRequestJsonSerializer = RegisterVirtualObjectIdJson.NameAcquisitionRequestFormat

  lazy implicit val NameAcquisitionAckJsonSerializer = RegisterVirtualObjectIdJson.NameAcquisitionAckFormat
  
  lazy implicit val NameAcquisitionRejectJsonSerializer = RegisterVirtualObjectIdJson.NameAcquisitionRejectFormat

  lazy implicit val RegisterVirtualObjectIdJsonSerializer = RegisterVirtualObjectIdJson.RegisterVirtualObjectIdReads

  lazy implicit val VirtualObjectBasicDefinitionJsonSerializer = VirtualObjectBasicDefinitionJson.VirtualObjectBasicDefinitionFormat

}
