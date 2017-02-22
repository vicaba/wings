package wings.user.domain.repository

import scala.concurrent.Future

import wings.toolkit.error.application.Types.RepositoryError
import wings.user.domain.User

import org.scalactic.{One, Or}

trait UserRepository {

  def findByEmailNameAndPassword(
      email: User.Email,
      name: User.Name,
      password: User.Password
  ): Future[Option[User]]

  def create(newUser: User): Future[User Or One[RepositoryError]]

}
