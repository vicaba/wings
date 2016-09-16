package wings.virtualobject.agent.infrastructure.event.repository.mongodb

import java.util.UUID

import play.api.libs.json.{Format, OFormat}
import reactivemongo.api.DB
import reactivemongo.play.json.collection.JSONCollection
import wings.toolkit.db.mongodb.service.MongoService
import wings.virtualobject.agent.domain.messages.event.VirtualObjectSensed
import wings.virtualobject.agent.infrastructure.keys.VirtualObjectOperatedKeys
import wings.virtualobject.agent.infrastructure.serialization.json.VirtualObjectOperatedJson

import scala.concurrent.ExecutionContext


case class VirtualObjectSensedMongoRepository
(
  db: DB
)
(
  implicit ec: ExecutionContext
)
  extends MongoService[VirtualObjectSensed, UUID](db) {

  override val collection: JSONCollection = db.collection("virtualObjectSensed")

  override def identityKey: String = VirtualObjectOperatedKeys.IdKey

  override def identityOf(o: VirtualObjectSensed): UUID = o.id

  override implicit val entityFormat: OFormat[VirtualObjectSensed] = VirtualObjectOperatedJson.VirtualObjectSensedFormat

  override implicit val identityFormat: Format[UUID] = Format(play.api.libs.json.Reads.uuidReads, play.api.libs.json.Writes.UuidWrites)


}
