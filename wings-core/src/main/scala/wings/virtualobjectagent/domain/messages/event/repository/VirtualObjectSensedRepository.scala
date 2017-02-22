package wings.virtualobjectagent.domain.messages.event.repository

import scala.concurrent.Future

import wings.toolkit.db.ClauseValues.SortOrder.SortOrder
import wings.toolkit.error.application.Types.RepositoryError
import wings.virtualobject.domain.VirtualObject
import wings.virtualobjectagent.domain.messages.event.VirtualObjectSensed

import org.scalactic.{One, Or}

trait VirtualObjectSensedRepository {

  def create(newVirtualObjectSensed: VirtualObjectSensed): Future[VirtualObjectSensed Or One[RepositoryError]]

  def findAll(): Future[List[VirtualObjectSensed]]

  def findAll(virtualObjectId: VirtualObject.IdType,
              sortOrder: Option[SortOrder],
              skip: Option[Int],
              limit: Option[Int]): Future[List[VirtualObjectSensed]]

}
