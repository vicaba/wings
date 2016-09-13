package wings.toolkit.db.mongodb.service

import reactivemongo.api.{DB, MongoDriver}



trait MongoEnvironment {

  val driver1: MongoDriver // first pool

  val mainDb: DB

}
