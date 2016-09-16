package wings.virtualobject.agent.infrastructure.event.repository.mongodb

import java.time.ZoneId
import java.util.UUID

import org.scalactic.{Bad, Good, One, Or}
import play.api.libs.json.{Format, OFormat}
import reactivemongo.api.DB
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDateTime
import reactivemongo.bson._
import reactivemongo.play.json.collection.JSONCollection
import wings.toolkit.db.mongodb.service.MongoService
import wings.toolkit.error.application.Types.RepositoryError
import wings.toolkit.error.application.Types.RepositoryError.CustomRepositoryError
import wings.virtualobject.agent.domain.messages.event.VirtualObjectSensed
import wings.virtualobject.agent.infrastructure.event.keys.VirtualObjectOperatedKeys
import wings.virtualobject.agent.infrastructure.event.serialization.json.VirtualObjectOperatedJson

import scala.concurrent.{ExecutionContext, Future}


case class VirtualObjectSensedMongoRepository
(
  db: DB
)
(
  implicit ec: ExecutionContext
)
  extends MongoService[VirtualObjectSensed, UUID](db) {

  override val collection: JSONCollection = db.collection("virtualObjectSensed")

  val bsonCollection: BSONCollection = db.collection("virtualObjectSensed")

  override def identityKey: String = VirtualObjectOperatedKeys.IdKey

  override def identityOf(o: VirtualObjectSensed): UUID = o.id

  override implicit val entityFormat: OFormat[VirtualObjectSensed] = VirtualObjectOperatedJson.VirtualObjectSensedFormat

  override implicit val identityFormat: Format[UUID] = Format(play.api.libs.json.Reads.uuidReads, play.api.libs.json.Writes.UuidWrites)


  // TODO: TIME CONVERSION GENERALIZATION
  override def create(o: VirtualObjectSensed): Future[Or[VirtualObjectSensed, One[RepositoryError]]] = {
    val document = transform(o)

    bsonCollection.insert(document).map {
      case wr if wr.ok => Good(o)
      case wr => Bad(One(CustomRepositoryError(wr.message)))
    }.recover {
      case wr: WriteResult => Bad(One(CustomRepositoryError(wr.message)))
    }
  }

  private def transform(o: VirtualObjectSensed): BSONDocument = {
    val doc = reactivemongo.play.json.ImplicitBSONHandlers.JsObjectWriter.write(entityFormat.writes(o))

    val bsonCreationDate =
      BSONDateTime(o.creationDate.getMillis)

    val convertedBsonDocument =
      BSONDocument(
        VirtualObjectOperatedKeys.CreationTimeKey -> bsonCreationDate)

    doc -- VirtualObjectOperatedKeys.CreationTimeKey ++ convertedBsonDocument
  }
}

object Main {
  def main(args: Array[String]): Unit = {
    println(ZoneId.getAvailableZoneIds)
  }
}
