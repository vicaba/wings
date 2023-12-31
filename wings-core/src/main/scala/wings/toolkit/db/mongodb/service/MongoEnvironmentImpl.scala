package wings.toolkit.db.mongodb.service

import scala.concurrent.ExecutionContext.Implicits.global

import wings.config.DependencyInjector._

import reactivemongo.api.{DB, MongoConnectionOptions, MongoDriver}
import scaldi.Injectable._

object MongoEnvironmentImpl extends MongoEnvironment {

  override val driver1 = MongoDriver() // first pool

  override lazy val mainDb: DB = {
    val connection = driver1.connection(inject[List[String]](identified by 'MongoDBServers), MongoConnectionOptions())
    connection(inject[String](identified by 'MainMongoDatabase))
  }
}
