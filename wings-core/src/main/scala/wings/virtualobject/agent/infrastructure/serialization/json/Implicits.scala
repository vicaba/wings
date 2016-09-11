package wings.virtualobject.agent.infrastructure.serialization.json

object Implicits {

  lazy implicit val ActuateOnVirtualObjectJsonSerializer = OperateOnVirtualObjectJson.ActuateOnVirtualObjectFormat

  lazy implicit val WatchVirtualObjectJsonSerializer = OperateOnVirtualObjectJson.WatchVirtualObjectFormat

  lazy implicit val VirtualObjectActuatedJsonSerializer = VirtualObjectOperatedJson.VirtualObjectActuatedFormat

  lazy implicit val VirtualObjectSensedJsonSerializer = VirtualObjectOperatedJson.VirtualObjectSensedFormat

}
