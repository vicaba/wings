package wings.virtualobject.infrastructure.repository.mongodb

import java.util.UUID

import reactivemongo.api.DB
import reactivemongo.play.json.collection.JSONCollection
import wings.model.IdentityManager
import wings.toolkit.db.mongodb.service.MongoCrudRepository
import wings.virtualobject.domain.VirtualObject
import wings.virtualobject.infrastructure.serialization.json.Implicits._

import scala.concurrent.ExecutionContext

object VOIdentityManager extends IdentityManager[VirtualObject, UUID] {

  override def name: String = "_id"

  override def next: UUID = UUID.randomUUID()

  override def of(entity: VirtualObject): Option[UUID] = entity.id
}

case class VirtualObjectMongoRepository
(
  db: DB
)
(
  identityManger: IdentityManager[VirtualObject, UUID]
)
(
  implicit ec: ExecutionContext
)
  extends MongoCrudRepository[VirtualObject, UUID](identityManger) {

  override val collection: JSONCollection = db.collection("virtualObjects")

}