package wings.model.lookup.database.mongodb

import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.DB
import reactivemongo.api.commands.WriteResult
import wings.model.lookup.Lookup
import wings.model.lookup.JSONImplicits._


import scala.concurrent.{ExecutionContext, Future}

/**
 *
 * @param db
 * @param collectionName
 * @param ec
 */
class LookupService
(val db: DB, collectionName: String = "lookups")
(implicit ec: ExecutionContext = scala.concurrent.ExecutionContext.global) {

  val collection: JSONCollection = db.collection(collectionName)

  def create[T <: Lookup](lookup: T): Future[Either[WriteResult, T]] = {
    collection.insert(lookup).map {
      case wr if wr.ok => Right(lookup)
      case wr => Left(wr)
    }
  }
}
