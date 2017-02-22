package wings.user.infrastructure.repository

import scala.concurrent.Future

import wings.toolkit.error.application.Types.RepositoryError
import wings.user.domain.User
import wings.user.domain.User.{Email, Name, Password}
import wings.user.domain.repository.UserRepository
import wings.user.infrastructure.repository.mongodb.UserMongoRepository

import org.scalactic.{One, Or}

case class UserRepositoryImpl(
    webUserMongoRepository: UserMongoRepository
) extends UserRepository {

  override def findByEmailNameAndPassword(email: Email, name: Name, password: Password): Future[Option[User]] =
    webUserMongoRepository.findByEmailNameAndPassword(email, name, password)

  override def create(newUser: User): Future[Or[User, One[RepositoryError]]] =
    webUserMongoRepository.create(newUser)

}
