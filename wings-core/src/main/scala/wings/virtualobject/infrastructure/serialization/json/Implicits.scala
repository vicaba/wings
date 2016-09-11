package wings.virtualobject.infrastructure.serialization.json

object Implicits {

  lazy implicit val ActuateStateJsonSerializer = ActuateStateJson.ActuateStateFormat

  lazy implicit val ActuateCapabilityJsonSerializer = ActuateCapabilityJson.ActuateCapabilityFormat

  lazy implicit val SenseCapabilityJsonSerializer = SenseCapabilityJson.SenseCapabilityFormat

  lazy implicit val VirtualObjectJsonSerializer = VirtualObjectJson.VirtualObjectFormat

}
