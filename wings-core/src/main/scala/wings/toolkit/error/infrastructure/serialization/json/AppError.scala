package wings.toolkit.error.infrastructure.serialization.json

import play.api.libs.json._
import wings.toolkit.error.application.Types
import wings.toolkit.error.application.Types.AppError
import wings.toolkit.error.infrastructure.keys.AppErrorKeys

// JSON library

import play.api.libs.json.Reads._

// Custom validation helpers

import play.api.libs.functional.syntax._

object AppError {

  object AppErrorWrites extends Writes[AppError] {

    override def writes(o: AppError): JsValue = Json.obj(AppErrorKeys.MessageKey -> o.message)

  }

}
