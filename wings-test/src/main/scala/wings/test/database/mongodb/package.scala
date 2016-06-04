package wings.test.database

import com.typesafe.config.ConfigFactory
import play.api.libs.json.Json
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{DB, MongoConnectionOptions, MongoDriver}
import reactivemongo.play.json.collection.JSONCollection
import play.modules.reactivemongo.json._
import wings.model.virtual.virtualobject.VOIdentityManager
import wings.model.virtual.virtualobject.services.db.mongo.VirtualObjectMongoService

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


package object mongodb {

  object MongoEnvironment extends wings.services.db.MongoEnvironment {

    override val driver1 = MongoDriver() // first pool

    override lazy val config = ConfigFactory.load

    override lazy val db1: DB = {
      val config = ConfigFactory.load("app")
      val connection = driver1.connection(config.getStringList("mongodb.servers"), MongoConnectionOptions())
      connection(config.getString("mongodb.db"))
    }
  }

  def cleanMongoDatabase: Future[WriteResult] = {
    val selector = Json.obj()
    val virtualObjectService = new VirtualObjectMongoService(MongoEnvironment.db1)(VOIdentityManager)
    val userCollection: JSONCollection = MongoEnvironment.db1.collection("webusers")
    virtualObjectService.delete(Json.obj())
    userCollection.remove(Json.obj())

    val sensedCollection: JSONCollection = MongoEnvironment.db1.collection("sensed")
    sensedCollection.remove(Json.obj())
  }
}
