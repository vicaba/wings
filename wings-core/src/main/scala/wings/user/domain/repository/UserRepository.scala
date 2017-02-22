package wings.user.domain.repository

import org.scalactic.{One, Or}
import wings.toolkit.error.application.Types.RepositoryError
import wings.user.domain.User

import scala.concurrent.Future

trait UserRepository {

  def findByEmailNameAndPassword(
      email: User.Email,
      name: User.Name,
      password: User.Password
  ): Future[Option[User]]

  def create(newUser: User): Future[User Or One[RepositoryError]]

}
