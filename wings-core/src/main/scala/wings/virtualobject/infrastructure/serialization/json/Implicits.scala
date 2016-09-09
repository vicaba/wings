package wings.virtualobject.infrastructure.serialization.json

import wings.virtualobject.agent.infrastructure.serialization.json.OperateOnVirtualObjectJson


object Implicits {

  lazy implicit val SenseCapabilityJsonSerializer = SenseCapabilityJson.SenseCapabilityFormat

  lazy implicit val VirtualObjectJsonSerializer = VirtualObjectJson.VirtualObjectFormat

}
