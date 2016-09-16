package wings.virtualobject.infrastructure.messages.event.repository

import org.scalactic.{One, Or}
import wings.toolkit.error.application.Types.RepositoryError
import wings.virtualobject.domain.messages.event.VirtualObjectSensed
import wings.virtualobject.domain.messages.event.repository.VirtualObjectSensedRepository
import wings.virtualobject.infrastructure.messages.event.repository.mongodb.VirtualObjectSensedMongoRepository

import scala.concurrent.Future


case class VirtualObjectSensedRepositoryImpl
(
  virtualObjectSensedMongoRepository: VirtualObjectSensedMongoRepository
)
 extends VirtualObjectSensedRepository
{
  override def create(newVirtualObjectSensed: VirtualObjectSensed): Future[Or[VirtualObjectSensed, One[RepositoryError]]] =
    virtualObjectSensedMongoRepository.create(newVirtualObjectSensed)

  override def findAll(): Future[List[VirtualObjectSensed]] =
    virtualObjectSensedMongoRepository.findAll()
}