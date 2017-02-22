package wings.virtualobject.domain.repository

import scala.concurrent.Future

import wings.toolkit.error.application.Types.RepositoryError
import wings.virtualobject.domain.VirtualObject

import org.scalactic.{One, Or}

trait VirtualObjectRepository {

  def findById(id: VirtualObject.IdType): Future[Option[VirtualObject]]

  def findAll(): Future[List[VirtualObject]]

  def findAll(skip: Option[Int], limit: Option[Int]): Future[List[VirtualObject]]

  def create(newVirtualObject: VirtualObject): Future[VirtualObject Or One[RepositoryError]]

}
