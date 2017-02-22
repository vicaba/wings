package wings.virtualobject.infrastructure.repository.mongodb

import scala.concurrent.ExecutionContext

import play.api.libs.json.{Format, OFormat}

import wings.toolkit.db.mongodb.service.MongoService
import wings.virtualobject.domain.VirtualObject
import wings.virtualobject.domain.VirtualObject.IdType
import wings.virtualobject.infrastructure.keys.VirtualObjectKeys
import wings.virtualobject.infrastructure.serialization.json.VirtualObjectJson

import reactivemongo.api.DB
import reactivemongo.play.json.collection.JSONCollection

case class VirtualObjectMongoRepository(
    db: DB
)(
    implicit ec: ExecutionContext
) extends MongoService[VirtualObject, VirtualObject.IdType](db) {

  override val collection: JSONCollection = db.collection("virtualObject")

  override def identityKey: String = VirtualObjectKeys.IdKey

  override def identityOf(o: VirtualObject): IdType = o.id

  override implicit val entityFormat: OFormat[VirtualObject] = VirtualObjectJson.VirtualObjectFormat

  override implicit val identityFormat: Format[IdType] =
    Format(play.api.libs.json.Reads.uuidReads, play.api.libs.json.Writes.UuidWrites)

}
