package wings.toolkit.error.application

object Types {

  sealed trait AppError {
    val message: String
  }

  sealed trait FormatError extends AppError

  object FormatError {

    val UUID = MalformedUUID

    case object MalformedUUID extends FormatError {
      override val message: String = "Malformed UUID"
    }

  }

  sealed trait RepositoryError extends AppError

  object RepositoryError {

    case class CustomRepositoryError(override val message: String) extends RepositoryError

    case object UnknownRepositoryError extends RepositoryError {
      override val message: String = "Unknown repository error"
    }

  }

  sealed trait ValueObjectError extends AppError

  object ValueObjectError {

    case class ValueObjectConstructionError(valueObjectName: Option[String]) extends ValueObjectError {

      override val message: String = valueObjectName
        .map {
          _ + "constraints error"
        }
        .getOrElse("ValueObject constraints error")

    }

  }

}
