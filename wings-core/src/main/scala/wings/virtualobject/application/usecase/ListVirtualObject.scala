package wings.virtualobject.application.usecase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import wings.toolkit.error.application.Types.AppError
import wings.virtualobject.domain.repository.VirtualObjectRepository
import wings.virtualobject.domain.VirtualObject

import org.scalactic.{Every, Good, Or}

object ListVirtualObject {

  case class Message(skip: Option[Int], limit: Option[Int])

  case class UseCase(virtualObjectRepository: VirtualObjectRepository) {

    def execute(message: ListVirtualObject.Message): Future[List[VirtualObject] Or Every[AppError]] =
      virtualObjectRepository.findAll(message.skip, message.limit).map(Good(_))

  }

}
