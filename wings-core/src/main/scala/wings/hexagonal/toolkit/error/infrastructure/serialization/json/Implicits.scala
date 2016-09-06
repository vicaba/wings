package wings.hexagonal.toolkit.error.infrastructure.serialization.json

object Implicits {

  implicit val AppErrorJsonSerializer = AppError.AppErrorWrites

}
