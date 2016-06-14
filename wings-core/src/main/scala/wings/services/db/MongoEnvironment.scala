package wings.services.db

import com.typesafe.config.Config
import reactivemongo.api.{DB, MongoDriver}



trait MongoEnvironment {

  val driver1: MongoDriver // first pool

  val mainDb: DB

}
