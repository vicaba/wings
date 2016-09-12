package wings.user.infrastructure.http.api

import com.google.inject.Singleton
import httpplay.config.DependencyInjector._
import httpplay.error.HttpErrorHandler
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}
import scaldi.Injectable._
import wings.user.application.usecase.SignUpUser
import wings.user.application.usecase.SignUpUser.Message

import scala.concurrent.Future


@Singleton
class LogoutUserController
  extends Controller {

  def apply() = Action.async(parse.anyContent) {
    implicit request =>
      Future.successful(Ok.withNewSession)
  }
}
