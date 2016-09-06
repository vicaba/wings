package wings.virtualobject.domain.repository

import java.util.UUID

import wings.model.virtual.virtualobject.VO

import scala.concurrent.Future

trait VirtualObjectRepository {

  def findById(id: UUID): Future[Option[VO]]

  def findAll(): Future[List[VO]]

  def findAll(skip: Option[Int], limit: Option[Int]): Future[List[VO]]

}
