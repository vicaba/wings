package wings.virtualobject.infrastructure.repository.mongodb

import java.util.UUID

import reactivemongo.api.DB
import reactivemongo.play.json.collection.JSONCollection
import wings.model.IdentityManager
import wings.model.virtual.virtualobject.VO
import wings.toolkit.db.mongodb.service.MongoCrudRepository

import scala.concurrent.ExecutionContext

case class VirtualObjectMongoRepository
(
  db: DB
)
(
  identityManger: IdentityManager[VO, UUID]
)
(
  implicit ec: ExecutionContext
)
  extends MongoCrudRepository[VO, UUID](identityManger) {

  override val collection: JSONCollection = db.collection("virtualObjects")

}