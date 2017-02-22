package wings.virtualobject.application.usecase

import java.util.UUID

import org.scalactic._
import wings.toolkit.error.application.Types.{AppError, FormatError}
import wings.virtualobject.domain.repository.VirtualObjectRepository
import wings.virtualobject.domain.VirtualObject

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * ShowVirtualObject UseCase
  *
  * Requests the VirtualObject metadata
  */
object ShowVirtualObject {

  case class Message(virtualObjectId: String)

  case class UseCase(virtualObjectRepository: VirtualObjectRepository) {

    def execute(message: ShowVirtualObject.Message): Future[Option[VirtualObject] Or Every[AppError]] = {
      Try(UUID.fromString(message.virtualObjectId)) match {
        case Success(id) => virtualObjectRepository.findById(id).map(Good(_))
        case Failure(e)  => Future.successful(Bad(One(FormatError.UUID)))
      }
    }

  }

}
