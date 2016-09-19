package wings.virtualobjectagent.application.usecase

import java.util.UUID

import org.scalactic._
import wings.toolkit.db.ClauseValues.SortOrder.SortOrder
import wings.toolkit.error.application.Types.{AppError, FormatError}
import wings.virtualobjectagent.domain.messages.event.VirtualObjectSensed
import wings.virtualobjectagent.domain.messages.event.repository.VirtualObjectSensedRepository

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}


object ListVirtualObjectSensed {

  case class Message(virtualObjectId: String, sortOrder: Option[SortOrder], skip: Option[Int], limit: Option[Int])

  case class UseCase(virtualObjectSensedRepository: VirtualObjectSensedRepository) {

    def execute(message: ListVirtualObjectSensed.Message): Future[List[VirtualObjectSensed] Or Every[AppError]] =
      Try(UUID.fromString(message.virtualObjectId)) match {
        case Success(id) => virtualObjectSensedRepository.findAll(id, message.sortOrder, message.skip, message.limit).map(Good(_))
        case Failure(e) => Future.successful(Bad(One(FormatError.UUID)))
      }
  }
}
