package wings.virtualobject.domain.repository

import org.scalactic.{One, Or}
import wings.toolkit.db.ClauseValues.SortOrder.{Ascendant, SortOrder}
import wings.toolkit.error.application.Types.RepositoryError
import wings.virtualobject.domain.VirtualObject

import scala.concurrent.Future

trait VirtualObjectRepository {

  def findById(id: VirtualObject.IdType): Future[Option[VirtualObject]]

  def findAll(): Future[List[VirtualObject]]

  def findAll(skip: Option[Int], limit: Option[Int]): Future[List[VirtualObject]]

  def create(newVirtualObject: VirtualObject): Future[VirtualObject Or One[RepositoryError]]

}
