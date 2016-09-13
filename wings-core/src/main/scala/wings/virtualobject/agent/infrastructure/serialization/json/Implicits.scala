package wings.virtualobject.agent.infrastructure.serialization.json

object Implicits {

  lazy implicit val ActuateOnVirtualObjectJsonSerializer = OperateOnVirtualObjectJson.ActuateOnVirtualObjectFormat

  lazy implicit val WatchVirtualObjectJsonSerializer = OperateOnVirtualObjectJson.WatchVirtualObjectFormat

  lazy implicit val VirtualObjectActuatedJsonSerializer = VirtualObjectOperatedJson.VirtualObjectActuatedFormat

  lazy implicit val VirtualObjectSensedJsonSerializer = VirtualObjectOperatedJson.VirtualObjectSensedFormat
  
  lazy implicit val NameAcquisitionRequestJsonSerializer = RegisterVirtualObjectJson.NameAcquisitionRequestFormat

  lazy implicit val NameAcquisitionAckJsonSerializer = RegisterVirtualObjectJson.NameAcquisitionAckFormat
  
  lazy implicit val NameAcquisitionRejectJsonSerializer = RegisterVirtualObjectJson.NameAcquisitionRejectFormat

  lazy implicit val RegisterVirtualObjectJsonSerializer = RegisterVirtualObjectJson.RegisterVirtualObjectReads

}
