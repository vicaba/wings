package wings.user.infrastructure.repository.mongodb

import play.api.libs.json.{Format, Json, OFormat}
import reactivemongo.api.DB
import reactivemongo.play.json.collection.JSONCollection
import wings.toolkit.db.mongodb.service.MongoService
import wings.user.domain.User
import wings.user.domain.User.IdType
import wings.user.infrastructure.keys.UserKeys
import wings.user.infrastructure.serialization.json.WebUserJson

import scala.concurrent.{ExecutionContext, Future}


case class UserMongoRepository(db: DB)(implicit ec: ExecutionContext) extends MongoService[User, User.IdType](db)
{

  override val collection: JSONCollection = db.collection("webUser")

  override def identityKey: String = UserKeys.IdKey

  override def identityOf(o: User): IdType = o.id

  override implicit val entityFormat: OFormat[User] = WebUserJson.WebUserFormat

  override implicit val identityFormat: Format[IdType] =
    Format(play.api.libs.json.Reads.uuidReads, play.api.libs.json.Writes.UuidWrites)

  def findByEmailNameAndPassword
  (
    email: User.Email,
    name: User.Name,
    password: User.Password
  ): Future[Option[User]] =
  {
    val criteria = Json.obj(
      UserKeys.EmailKey -> email.value,
      UserKeys.NameKey -> name.value,
      UserKeys.PasswordKey -> password.value
    )
    findOneByCriteria(criteria)
  }

}
