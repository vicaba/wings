package database.mongodb

import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConversions._


import reactivemongo.api.{DB, MongoConnectionOptions, MongoDriver}

object MongoEnvironment extends wings.services.db.MongoEnvironment {

  override val driver1 = MongoDriver() // first pool

  override lazy val config = ConfigFactory.load

  override lazy val db1: DB = {
    val config = ConfigFactory.load("application")
    val connection = driver1.connection(config.getStringList("mongodb.servers"), MongoConnectionOptions())
    connection.db(config.getString("mongodb.db"))
  }
}
