package wings.user.infrastructure.serialization.json

object Implicits {

  implicit lazy val WebUserJsonSerializer = WebUserJson.WebUserFormat

}
