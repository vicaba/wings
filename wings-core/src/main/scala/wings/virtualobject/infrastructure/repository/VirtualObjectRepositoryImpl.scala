package wings.virtualobject.infrastructure.repository

import org.scalactic.{One, Or}
import wings.toolkit.error.application.Types.AppError
import wings.virtualobject.domain.VirtualObject
import wings.virtualobject.domain.repository.VirtualObjectRepository
import wings.virtualobject.infrastructure.repository.mongodb.VirtualObjectMongoRepository

import scala.concurrent.Future

case class VirtualObjectRepositoryImpl
(
  virtualObjectMongoRepository: VirtualObjectMongoRepository
)
  extends VirtualObjectRepository {

  override def findById(id: VirtualObject.IdType): Future[Option[VirtualObject]] =
    virtualObjectMongoRepository.findById(id)

  override def findAll(): Future[List[VirtualObject]] =
    virtualObjectMongoRepository.findAll()


  override def findAll(skip: Option[Int], limit: Option[Int]): Future[List[VirtualObject]] =
    virtualObjectMongoRepository.findAll(skip, limit)

  override def create(newVirtualObject: VirtualObject): Future[VirtualObject Or One[AppError]] =
    virtualObjectMongoRepository.create(newVirtualObject)
}
