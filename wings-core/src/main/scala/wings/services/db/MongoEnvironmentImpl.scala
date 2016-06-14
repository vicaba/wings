package wings.services.db

import reactivemongo.api.{DB, MongoConnectionOptions, MongoDriver}
import scaldi.Injectable._
import wings.config.DependencyInjector._

import scala.concurrent.ExecutionContext.Implicits.global



object MongoEnvironmentImpl extends wings.services.db.MongoEnvironment {

  override val driver1 = MongoDriver() // first pool

  override lazy val mainDb: DB = {
    val connection = driver1.connection(inject[List[String]](identified by 'MongoDBServers), MongoConnectionOptions())
    connection(inject[String](identified by 'MainMongoDatabase))
  }
}