package wings.virtualobjectagent.infrastructure.messages.event.repository.mongodb

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

import play.api.libs.json.{Format, OFormat}

import wings.toolkit.db.ClauseValues.SortOrder.{Ascendant, Descendant, SortOrder, SortOrderWithKey}
import wings.toolkit.db.mongodb.service.MongoService
import wings.toolkit.error.application.Types.RepositoryError
import wings.toolkit.error.application.Types.RepositoryError.CustomRepositoryError
import wings.virtualobject.domain.VirtualObject
import wings.virtualobjectagent.domain.messages.event.VirtualObjectSensed
import wings.virtualobjectagent.infrastructure.messages.event.keys.VirtualObjectOperatedKeys
import wings.virtualobjectagent.infrastructure.messages.event.serialization.json.VirtualObjectOperatedJson

import org.scalactic.{Bad, Good, One, Or}
import reactivemongo.api.{DB, QueryOpts, ReadPreference}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson._
import reactivemongo.bson.BSONDateTime
import reactivemongo.play.json.BSONFormats.BSONDocumentFormat
import reactivemongo.play.json.collection.JSONCollection

case class VirtualObjectSensedMongoRepository(
    db: DB
)(
    implicit ec: ExecutionContext
) extends MongoService[VirtualObjectSensed, UUID](db) {

  override val collection: JSONCollection = db.collection("virtualObjectSensed")

  val bsonCollection: BSONCollection = db.collection("virtualObjectSensed")

  override def identityKey: String = VirtualObjectOperatedKeys.IdKey

  override def identityOf(o: VirtualObjectSensed): UUID = o.id

  override implicit val entityFormat: OFormat[VirtualObjectSensed] =
    VirtualObjectOperatedJson.VirtualObjectSensedFormat

  override implicit val identityFormat: Format[UUID] =
    Format(play.api.libs.json.Reads.uuidReads, play.api.libs.json.Writes.UuidWrites)

  // TODO: TIME CONVERSION GENERALIZATION
  override def create(o: VirtualObjectSensed): Future[Or[VirtualObjectSensed, One[RepositoryError]]] = {
    val document = transform(o)

    bsonCollection
      .insert(document)
      .map {
        case wr if wr.ok => Good(o)
        case wr          => Bad(One(CustomRepositoryError(wr.message)))
      }
      .recover {
        case wr: WriteResult => Bad(One(CustomRepositoryError(wr.message)))
      }
  }

  def findAll(virtualObjectId: VirtualObject.IdType,
              sortOrder: Option[SortOrderWithKey],
              skip: Option[Int],
              limit: Option[Int]): Future[List[VirtualObjectSensed]] = {
    val findCriteria = BSONDocument(VirtualObjectOperatedKeys.VirtualObjectIdKey -> virtualObjectId.toString)

    val skipN  = skip.getOrElse(0)
    val limitN = limit.getOrElse(0)

    val v = (sortOrder.map { sort =>
      bsonCollection.find(findCriteria).sort(sortOrderCriteriaBson(sort))
    } getOrElse
      bsonCollection.find(findCriteria))
      .options(QueryOpts(skipN, limitN))
      .cursor[BSONDocument](readPreference = ReadPreference.primary)
      .collect[List]()

    v.map(_.map(transform))
  }

  override def findAll(): Future[List[VirtualObjectSensed]] = {
    val v =
      bsonCollection.find(BSONDocument()).cursor[BSONDocument](readPreference = ReadPreference.primary).collect[List]()

    v.map(_.map(transform))

  }

  private def transform(doc: BSONDocument): VirtualObjectSensed = {

    val convertedBsonDocument = {

      doc.get(VirtualObjectOperatedKeys.CreationTimeKey).map { bsonCreationTime =>
        val convertedBsonDocument =
          BSONDocument(
            VirtualObjectOperatedKeys.CreationTimeKey -> BSONLong(bsonCreationTime.asInstanceOf[BSONDateTime].value))

        doc -- VirtualObjectOperatedKeys.CreationTimeKey ++ convertedBsonDocument
      }

    }.get

    BSONDocumentFormat.writes(convertedBsonDocument).as[VirtualObjectSensed]
  }

  private def transform(o: VirtualObjectSensed): BSONDocument = {
    val doc = reactivemongo.play.json.ImplicitBSONHandlers.JsObjectWriter.write(entityFormat.writes(o))

    val bsonCreationDate =
      BSONDateTime(o.creationDate.getMillis)

    val convertedBsonDocument =
      BSONDocument(VirtualObjectOperatedKeys.CreationTimeKey -> bsonCreationDate)

    doc -- VirtualObjectOperatedKeys.CreationTimeKey ++ convertedBsonDocument
  }

  private def sortOrderCriteriaBson(sortOrder: SortOrderWithKey): BSONDocument = sortOrder.sortOrder match {
    case Ascendant  => BSONDocument(sortOrder.key -> 1)
    case Descendant => BSONDocument(sortOrder.key -> -1)
  }
}
