package wings.virtualobject.infrastructure.serialization.json


object Implicits {

  lazy implicit val SenseCapabilityJsonSerializer = SenseCapabilityJson.SenseCapabilityFormat

  lazy implicit val VirtualObjectJsonSerializer = VirtualObjectJson.VirtualObjectFormat

}
