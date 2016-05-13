package models.user.services.db.mongo

import java.util.UUID

import models.user.WebUser
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.DB
import wings.model.IdentityManager
import wings.services.db.CRUDService

import scala.concurrent.ExecutionContext

class UserMongoService(db: DB)(identityManger: IdentityManager[WebUser, UUID])(implicit ec: ExecutionContext) extends CRUDService[WebUser, UUID](identityManger) {
  override val collection: JSONCollection = db.collection("webusers")
}
