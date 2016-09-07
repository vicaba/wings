package wings.toolkit.error.infrastructure.serialization.json

object Implicits {

  implicit val AppErrorJsonSerializer = AppErrorJson.AppErrorWrites

}
