package wings.model.virtual.virtualobject.services.db.mongo

import java.util.UUID

import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.DB
import wings.model.IdentityManager
import wings.model.virtual.virtualobject.sensed.SensedValue
import wings.services.db.CRUDService

import scala.concurrent.ExecutionContext


case class SensedValueMongoService(db: DB)(identityManger: IdentityManager[SensedValue, UUID])(implicit ec: ExecutionContext) extends CRUDService[SensedValue, UUID](identityManger) {
  override val collection: JSONCollection = db.collection("sensed")
}