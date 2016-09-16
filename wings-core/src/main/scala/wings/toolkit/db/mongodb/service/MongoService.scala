package wings.toolkit.db.mongodb.service

import org.scalactic.{Bad, Good, One, Or}
import play.api.libs.iteratee.Enumerator
import play.api.libs.json._
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{DB, QueryOpts, ReadPreference}
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection
import wings.toolkit.error.application.Types.RepositoryError
import wings.toolkit.error.application.Types.RepositoryError.{CustomRepositoryError, UnknownRepositoryError}
import wings.virtualobject.agent.infrastructure.event.keys.VirtualObjectOperatedKeys

import scala.concurrent.{ExecutionContext, Future}

abstract class MongoService[E, ID]
(
  db: DB
)
(implicit
 ec: ExecutionContext
) {

  val collection: JSONCollection

  def identityKey: String

  def identityOf(o: E): ID

  implicit val entityFormat: OFormat[E]

  implicit val identityFormat: Format[ID]

  def findById(id: ID): Future[Option[E]] = {
    collection.find(Json.obj(identityKey -> id)).one[E]
  }

  def findStreamByCriteria(criteria: JsObject, limit: Int): Enumerator[E] = {
    collection.find(criteria).cursor[E](readPreference = ReadPreference.primary).enumerate(limit)
  }

  def findStreamByCriteria(criteria: JsObject): Enumerator[E] = {
    collection.find(criteria).cursor[E](readPreference = ReadPreference.primary).enumerate()
  }

  def findByCriteria(criteria: JsObject, limit: Int): Future[Traversable[E]] = {
    collection.find(criteria).cursor[E](readPreference = ReadPreference.primary).collect[List](limit)
  }

  def findByCriteria(criteria: JsObject): Future[Traversable[E]] = {
    collection.find(criteria).cursor[E](readPreference = ReadPreference.primary).collect[List]()
  }

  def findOneByCriteria(criteria: JsObject): Future[Option[E]] = {
    collection.
      find(criteria).
      one[E]
  }

  def findAll(skip: Option[Int], limit: Option[Int]): Future[List[E]] = {
    val skipN = skip.getOrElse(0)
    val limitN = limit.getOrElse(0)
    collection.
      find(Json.obj()).
      options(QueryOpts(skipN, limitN)).
      cursor[E](readPreference = ReadPreference.primary).
      collect[List]()
  }

  def findAll(): Future[List[E]] = findAll(None, None)

  def create(o: E): Future[E Or One[RepositoryError]] = {
    collection.insert(o).map {
      case wr if wr.ok => Good(o)
      case wr => Bad(One(CustomRepositoryError(wr.message)))
    }.recover {
      case wr: WriteResult => Bad(One(CustomRepositoryError(wr.message)))
    }
  }

  def update(o: E): Future[Either[WriteResult, E]] = {
    collection.update(Json.obj(identityKey -> identityOf(o)), o).map {
      case wr if wr.ok => Right(o)
      case wr => Left(wr)
    }
  }

  def delete(id: ID): Future[Either[WriteResult, ID]] = {
    collection.remove(Json.obj(identityKey -> id)) map {
      case le if le.ok => Right(id)
      case le => Left(le)
    }
  }

  def delete(selector: JsObject): Future[WriteResult] = {
    collection.remove(selector)
  }
}

