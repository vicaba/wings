package wings.test.database

import play.api.libs.json.Json
import play.modules.reactivemongo.json._
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json.collection.JSONCollection
import scaldi.Injectable._
import wings.config.DependencyInjector._
import wings.model.virtual.virtualobject.VOIdentityManager
import wings.model.virtual.virtualobject.services.db.mongo.VirtualObjectMongoService
import wings.services.db.MongoEnvironment

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


package object mongodb {

  val mongoEnv: MongoEnvironment = inject[MongoEnvironment](identified by 'MongoEnvironment)

  def cleanMongoDatabase: Future[WriteResult] = {
    val selector = Json.obj()
    val virtualObjectService = new VirtualObjectMongoService(mongoEnv.mainDb)(VOIdentityManager)
    val userCollection: JSONCollection = mongoEnv.mainDb.collection("webusers")
    virtualObjectService.delete(Json.obj())
    userCollection.remove(Json.obj())

    val sensedCollection: JSONCollection = mongoEnv.mainDb.collection("sensed")
    sensedCollection.remove(Json.obj())
  }
}
