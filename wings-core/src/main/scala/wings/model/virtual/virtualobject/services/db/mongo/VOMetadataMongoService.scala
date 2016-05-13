package wings.model.virtual.virtualobject.services.db.mongo

import java.util.UUID

import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.DB
import wings.model.IdentityManager
import wings.model.virtual.virtualobject.metadata.VOMetadata
import wings.services.db.CRUDService

import scala.concurrent.ExecutionContext

/**
  * Created by vicaba on 12/10/15.
  */
case class VOMetadataMongoService(db: DB)(identityManger: IdentityManager[VOMetadata, UUID])(implicit ec: ExecutionContext) extends CRUDService[VOMetadata, UUID](identityManger) {
  override val collection: JSONCollection = db.collection("virtualObjectMetadata")
}