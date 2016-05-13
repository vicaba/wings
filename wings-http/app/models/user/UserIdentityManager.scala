package models.user

import java.util.UUID

import wings.model.IdentityManager

object UserIdentityManager extends IdentityManager[WebUser, UUID] {

  implicit val thisManager = UserIdentityManager

  override def name: String = "_id"

  override def next: UUID = UUID.randomUUID()

  override def of(entity: WebUser): Option[UUID] = entity.id
}
