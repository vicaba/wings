package wings.model.service.failure

import wings.model.failure.Failure
import wings.model.service.Service

trait ServiceFailure extends Failure[Service] {
  val service: Service
}

case class ServiceUnavailable(override val service: Service) extends ServiceFailure
