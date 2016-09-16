package wings.virtualobject.agent.domain.messages.event.repository

import org.scalactic.{One, Or}
import wings.toolkit.error.application.Types.RepositoryError
import wings.virtualobject.agent.domain.messages.event.VirtualObjectSensed

import scala.concurrent.Future


trait VirtualObjectSensedRepository {

  def create(newVirtualObjectSensed: VirtualObjectSensed): Future[VirtualObjectSensed Or One[RepositoryError]]

  def findAll(): Future[List[VirtualObjectSensed]]

}
