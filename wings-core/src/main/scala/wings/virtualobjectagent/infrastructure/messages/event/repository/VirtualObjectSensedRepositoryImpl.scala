package wings.virtualobjectagent.infrastructure.messages.event.repository

import scala.concurrent.Future

import wings.toolkit.db.ClauseValues.SortOrder.{SortOrder, SortOrderWithKey}
import wings.toolkit.error.application.Types.RepositoryError
import wings.virtualobject.domain.VirtualObject
import wings.virtualobjectagent.domain.messages.event.VirtualObjectSensed
import wings.virtualobjectagent.domain.messages.event.repository.VirtualObjectSensedRepository
import wings.virtualobjectagent.infrastructure.messages.event.keys.VirtualObjectOperatedKeys
import wings.virtualobjectagent.infrastructure.messages.event.repository.mongodb.VirtualObjectSensedMongoRepository

import org.scalactic.{One, Or}

case class VirtualObjectSensedRepositoryImpl(
    virtualObjectSensedMongoRepository: VirtualObjectSensedMongoRepository
) extends VirtualObjectSensedRepository {
  override def create(
      newVirtualObjectSensed: VirtualObjectSensed): Future[Or[VirtualObjectSensed, One[RepositoryError]]] =
    virtualObjectSensedMongoRepository.create(newVirtualObjectSensed)

  override def findAll(virtualObjectId: VirtualObject.IdType,
                       sortOrder: Option[SortOrder],
                       skip: Option[Int],
                       limit: Option[Int]): Future[List[VirtualObjectSensed]] = {
    val order = sortOrder.map(SortOrderWithKey(VirtualObjectOperatedKeys.CreationTimeKey, _))
    virtualObjectSensedMongoRepository.findAll(virtualObjectId, order, skip, limit)
  }

  override def findAll(): Future[List[VirtualObjectSensed]] =
    virtualObjectSensedMongoRepository.findAll()

}
