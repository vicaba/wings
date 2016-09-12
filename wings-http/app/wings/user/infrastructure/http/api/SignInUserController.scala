package wings.user.infrastructure.http.api

import com.google.inject.Singleton
import httpplay.config.DependencyInjector._
import httpplay.error.HttpErrorHandler
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}
import scaldi.Injectable._
import wings.user.application.usecase.SignInUser
import wings.user.application.usecase.SignInUser.Message
import wings.user.infrastructure.keys.UserKeys


import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SignInUserController
  extends Controller {

  val httpErrorHandler: HttpErrorHandler = inject[HttpErrorHandler](identified by 'HttpErrorHandler)

  val signInUserUseCase: SignInUser.UseCase = inject[SignInUser.UseCase](identified by 'SignInUserUseCase)

  def apply() = Action.async(parse.json) { implicit request =>
    userSignInForm.bindFromRequest().fold(
      formWithErrors => {
        Future {
          BadRequest
        }
      },
      success => {
        signInUserUseCase.execute(Message(success._1, success._2, success._3)).map {
          httpErrorHandler.handle(_) {
            case Some(user) => Created.addingToSession(UserKeys.IdKey -> user.id.toString, UserKeys.NameKey -> user.name.value)
            case None => NotFound
          }
        }
      }
    )
  }

  def userSignInForm = Form(
    tuple(
      "username" -> text,
      "email" -> email,
      "passwd" -> nonEmptyText(minLength = 2, maxLength = 50)
    )
  )

}
