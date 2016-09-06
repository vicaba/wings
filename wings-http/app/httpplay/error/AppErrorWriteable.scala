package httpplay.error

import akka.util.ByteString
import play.api.http.Writeable
import wings.toolkit.error.application.Types.AppError

object AppErrorWriteable {

  val appErrorWriteable = new Writeable[AppError](ae => ByteString(""), Some("text/plain"))

}
