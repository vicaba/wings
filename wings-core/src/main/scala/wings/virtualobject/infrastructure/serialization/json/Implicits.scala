package wings.virtualobject.infrastructure.serialization.json


object Implicits {

  implicit val VirtualObjectJsonSerializer = VirtualObjectJson.VirtualObjectFormat

}