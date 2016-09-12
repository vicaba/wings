package wings.user.infrastructure.serialization.json

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.{OFormat, OWrites, Reads, _}
import wings.user.domain.User
import wings.user.infrastructure.keys.UserKeys

object WebUserJson {

  val WebUserReads: Reads[User] = (
    (__ \ UserKeys.IdKey).read[UUID] and
      (__ \ UserKeys.EmailKey).read[String].map(User.Name(_)) and
      (__ \ UserKeys.NameKey).read[String].map(User.Email(_)) and
      (__ \ UserKeys.PasswordKey).read[String].map(User.Password(_))
    )(User.apply _)

  val WebUserWrites: OWrites[User] = (
    (__ \ UserKeys.IdKey).write[UUID] and
      (__ \ UserKeys.EmailKey).write[String].contramap { o: User.Name => o.value} and
      (__ \ UserKeys.NameKey).write[String].contramap { o: User.Email => o.value} and
      (__ \ UserKeys.PasswordKey).write[String].contramap { o: User.Password => o.value}
    )(unlift(User.unapply _))

  val WebUserFormat = OFormat(WebUserReads, WebUserWrites)

}