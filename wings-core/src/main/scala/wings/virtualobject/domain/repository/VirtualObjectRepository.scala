package wings.virtualobject.domain.repository

import java.util.UUID

import wings.virtualobject.domain.VirtualObject

import scala.concurrent.Future

trait VirtualObjectRepository {

  def findById(id: UUID): Future[Option[VirtualObject]]

  def findAll(): Future[List[VirtualObject]]

  def findAll(skip: Option[Int], limit: Option[Int]): Future[List[VirtualObject]]

}
