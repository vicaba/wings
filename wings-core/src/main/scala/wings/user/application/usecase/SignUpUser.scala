package wings.user.application.usecase

import org.scalactic.Accumulation._
import org.scalactic.{Every, Good, Or}
import wings.toolkit.error.application.Types.AppError
import wings.user.domain.User
import wings.user.domain.User.{Email, Name, Password}
import wings.user.domain.repository.UserRepository

import scala.concurrent.Future


object SignUpUser {

  case class Message(userName: String, userEmail: String, userPassword: String, userPasswordConfirmation: String)

  case class UseCase(private val userRepository: UserRepository) {

    def execute(message: Message): Future[User Or Every[AppError]] = {
      withGood(
        Name.fromString(message.userName),
        Email.fromString(message.userEmail),
        Password.fromString(message.userPassword),
        Password.fromString(message.userPassword)
      ) {
        (_, _, _, _)
      } match {
        case Good((name, email, password, _)) =>
          userRepository.create(User(User.nextIdentity, name, email, password))
        case bad => Future.successful(Good[User].orBad(bad.swap.get))
      }
    }

  }

}
