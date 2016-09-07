package wings.toolkit.db.mongodb.service

import play.api.libs.iteratee.Enumerator
import play.api.libs.json._
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{QueryOpts, ReadPreference}
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection
import wings.model.{HasIdentity, IdentityManager}

import scala.concurrent.{ExecutionContext, Future}

abstract class MongoCrudService[E <: HasIdentity[ID], ID](identityManager: IdentityManager[E, ID])
                                                         (implicit
                                                     tFormat: OFormat[E],
                                                     idFormat: Format[ID],
                                                     ec: ExecutionContext) {
  val collection: JSONCollection

  def findById(id: ID): Future[Option[E]] = {
    collection.find(Json.obj(identityManager.name -> id)).one[E]
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

  def create(o: E): Future[Either[WriteResult, E]] = {
    collection.insert(o).map {
      case wr if wr.ok => Right(o)
      case wr => Left(wr)
    }.recover {
      case wr: WriteResult => Left(wr)
    }
  }

  def update(o: E): Future[Either[WriteResult, E]] = {
    collection.update(Json.obj(identityManager.name -> o.id), o).map {
      case wr if wr.ok => Right(o)
      case wr => Left(wr)
    }
  }

  def delete(id: ID): Future[Either[WriteResult, ID]] = {
    collection.remove(Json.obj(identityManager.name -> id)) map {
      case le if le.ok => Right(id)
      case le => Left(le)
    }
  }

  def delete(selector: JsObject): Future[WriteResult] = {
    collection.remove(selector)
  }
}