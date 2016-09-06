package wings.hexagonal.virtualobject.domain.repository

import java.util.UUID

import wings.model.virtual.virtualobject.VO

import scala.concurrent.Future

trait VirtualObjectRepository {

  def findById(id: UUID): Future[Option[VO]]

}
