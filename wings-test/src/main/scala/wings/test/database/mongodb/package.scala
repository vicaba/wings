package wings.test.database

import play.api.libs.json.Json
import play.modules.reactivemongo.json._
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json.collection.JSONCollection
import scaldi.Injectable._
import wings.config.DependencyInjector._
import wings.virtualobject.infrastructure.repository.mongodb.{VOIdentityManager, VirtualObjectMongoRepository}
import wings.services.db.MongoEnvironment

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}


package object mongodb {

  val mongoEnv: MongoEnvironment = inject[MongoEnvironment](identified by 'MongoEnvironment)

  def cleanMongoDatabase: Future[WriteResult] = {
    val selector = Json.obj()
    val virtualObjectService = VirtualObjectMongoRepository(mongoEnv.mainDb)(VOIdentityManager)
    Thread.sleep(2000)

    val userCollection: JSONCollection = mongoEnv.mainDb.collection("webusers")
    virtualObjectService.delete(Json.obj())
    userCollection.remove(Json.obj())

    val sensedCollection: JSONCollection = mongoEnv.mainDb.collection("sensed")
    sensedCollection.remove(Json.obj())
  }
}
