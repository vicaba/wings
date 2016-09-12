package wings.user.application.usecase

import org.scalactic.Accumulation._
import org.scalactic._
import wings.toolkit.error.application.Types.AppError
import wings.user.domain.User
import wings.user.domain.User.{Email, Name, Password}
import wings.user.domain.repository.UserRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SignInUser {

  case class Message(userName: String, userEmail: String, userPassword: String)

  case class UseCase(private val userRepository: UserRepository) {

    def execute(message: Message): Future[Option[User] Or Every[AppError]] = {
      withGood(
        Name.fromString(message.userName),
        Email.fromString(message.userEmail),
        Password.fromString(message.userPassword)
      ) {
        (_, _, _)
      } match {
        case Good((name, email, password)) => userRepository.findByEmailNameAndPassword(email, name, password).map(Good(_))
        case bad => Future.successful(Good[Option[User]].orBad(bad.swap.get))
      }
    }
  }

}
