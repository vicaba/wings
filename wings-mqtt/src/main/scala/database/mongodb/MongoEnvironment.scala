package database.mongodb

import com.typesafe.config.ConfigFactory
import reactivemongo.api.{MongoConnectionOptions, DB, MongoDriver}
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global


object MongoEnvironment extends wings.services.db.MongoEnvironment {

  override val driver1 = MongoDriver() // first pool

  override lazy val config = ConfigFactory.load

  override lazy val db1: DB = {
    val config = ConfigFactory.load("app")
    val connection = driver1.connection(config.getStringList("mongodb.servers"), MongoConnectionOptions())
    connection(config.getString("mongodb.db"))
  }
}