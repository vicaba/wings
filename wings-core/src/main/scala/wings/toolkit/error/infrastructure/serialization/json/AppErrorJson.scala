package wings.toolkit.error.infrastructure.serialization.json

import play.api.libs.json._
import wings.toolkit.error.application.Types.AppError
import wings.toolkit.error.infrastructure.keys.AppErrorKeys

object AppErrorJson {

  object AppErrorWrites extends Writes[AppError] {

    override def writes(o: AppError): JsValue = Json.obj(AppErrorKeys.MessageKey -> o.message)

  }

}
