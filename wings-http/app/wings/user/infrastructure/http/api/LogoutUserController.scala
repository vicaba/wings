package wings.user.infrastructure.http.api

import scala.concurrent.Future

import play.api.mvc.{Action, AnyContent, Controller}

import com.google.inject.Singleton


@Singleton
class LogoutUserController extends Controller {

  def apply(): Action[AnyContent] = Action.async(parse.anyContent) { implicit request =>
    Future.successful(Ok.withNewSession)
  }
}
