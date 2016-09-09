package wings.virtualobject.agent.infrastructure.serialization.json

object Implicits {

  lazy implicit val ActuateOnVirtualObjectJsonSerializer = OperateOnVirtualObjectJson.ActuateOnVirtualObjectFormat

  lazy implicit val WatchVirtualObjectJsonSerializer = OperateOnVirtualObjectJson.WatchVirtualObjectFormat

}
