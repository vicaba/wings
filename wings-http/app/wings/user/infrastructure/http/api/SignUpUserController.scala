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
import play.api.i18n.Messages.Implicits._


import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class SignUpUserController
  extends Controller {

  val httpErrorHandler: HttpErrorHandler = inject[HttpErrorHandler](identified by 'HttpErrorHandler)

  val signUpUserUseCase: SignUpUser.UseCase = inject[SignUpUser.UseCase](identified by 'SignUpUserUseCase)

  def apply() = Action.async(parse.json) { implicit request =>
    println(request.body)
    userSignUpForm.bindFromRequest().fold(
      formWithErrors => {
        Future {
          BadRequest
        }
      },
      success => {
        signUpUserUseCase.execute(Message(success._1, success._2, success._3, success._4)).map {
          httpErrorHandler.handle(_) { user => Created }
        }
      }
    )
  }

  def userSignUpForm = Form(
    tuple(
      "username" -> text,
      "email" -> email,
      "passwd" -> nonEmptyText(minLength = 2, maxLength = 50),
      "passwdConf" -> text
    ).verifying("Passwords do not coincide", fields => fields._3 == fields._4)
  )

}
