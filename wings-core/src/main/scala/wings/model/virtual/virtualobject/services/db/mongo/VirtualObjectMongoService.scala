package wings.model.virtual.virtualobject.services.db.mongo

import java.util.UUID

import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.DB
import wings.model.IdentityManager
import wings.model.virtual.virtualobject.VO
import wings.services.db.CRUDService

import scala.concurrent.ExecutionContext

case class VirtualObjectMongoService(db: DB)(identityManger: IdentityManager[VO, UUID])(implicit ec: ExecutionContext) extends CRUDService[VO, UUID](identityManger) {
  override val collection: JSONCollection = db.collection("virtualObjects")
}