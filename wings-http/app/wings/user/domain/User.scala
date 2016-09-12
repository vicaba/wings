package wings.user.domain

import java.util.UUID

import org.scalactic.{Good, One, Or}
import wings.toolkit.error.application.Types.ValueObjectError.ValueObjectConstructionError
import wings.user.domain.User.{Email, Name, Password}


case class User
(
  id: User.IdType,
  name: Name,
  email: Email,
  password: Password
)

object User {

  type IdType = UUID

  def nextIdentity: IdType = UUID.randomUUID()

  case class Name(value: String)

  object Name {
    def fromString(value: String): Name Or One[ValueObjectConstructionError] = Good(new Name(value))
  }

  case class Email(value: String)

  object Email {
    def fromString(value: String): Email Or One[ValueObjectConstructionError] = Good(new Email(value))
  }

  case class Password(value: String)


  object Password {
    def fromString(value: String): Password Or One[ValueObjectConstructionError] = Good(new Password(value))
  }

}
