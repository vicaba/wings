package wings.virtualobject.infrastructure.repository

import java.util.UUID

import wings.virtualobject.domain.repository.VirtualObjectRepository
import wings.model.virtual.virtualobject.VO
import wings.model.virtual.virtualobject.services.db.mongo.VirtualObjectMongoService

import scala.concurrent.Future

case class VirtualObjectRepositoryImpl
(
  virtualObjectMongoRepository: VirtualObjectMongoService
)
  extends VirtualObjectRepository {

  override def findById(id: UUID): Future[Option[VO]] =
    virtualObjectMongoRepository.findById(id)

}
