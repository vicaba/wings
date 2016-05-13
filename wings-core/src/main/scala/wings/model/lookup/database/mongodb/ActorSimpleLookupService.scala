package wings.model.lookup.database.mongodb

import play.api.libs.json.JsObject
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.{ReadPreference, DB}
import play.modules.reactivemongo.json._
import reactivemongo.api.commands.WriteResult
import wings.model.lookup.ActorSimpleLookup
import wings.model.lookup.ActorSimpleLookupJSONImplicits._

import scala.concurrent.{ExecutionContext, Future}

/**
 *
 * @param db
 * @param collectionName
 * @param ec
 */
class ActorSimpleLookupService
(val db: DB, collectionName: String = "lookups")
(implicit ec: ExecutionContext = scala.concurrent.ExecutionContext.global) {

  val collection: JSONCollection = db.collection(collectionName)

  def findByCriteria(criteria: JsObject, limit: Int): Future[Traversable[ActorSimpleLookup]] = {
    collection.find(criteria).cursor[ActorSimpleLookup](readPreference = ReadPreference.primary).collect[List](limit)
  }
}