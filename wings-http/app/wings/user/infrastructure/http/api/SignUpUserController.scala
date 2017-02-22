package wings.user.infrastructure.http.api

import com.google.inject.{Inject, Provider, Singleton}
import httpplay.config.DependencyInjector._
import httpplay.error.HttpErrorHandler
import play.api.Application
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}
import scaldi.Injectable._
import wings.user.application.usecase.SignUpUser
import wings.user.application.usecase.SignUpUser.Message
import wings.user.infrastructure.keys.UserKeys

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SignUpUserController @Inject()(appProvider: Provider[Application]) extends Controller {

  val httpErrorHandler: HttpErrorHandler = inject[HttpErrorHandler](identified by 'HttpErrorHandler)

  val signUpUserUseCase: SignUpUser.UseCase = inject[SignUpUser.UseCase](identified by 'SignUpUserUseCase)

  def apply() = Action.async(parse.json) { implicit request =>
    userSignUpForm
      .bindFromRequest()
      .fold(
        formWithErrors => {
          implicit lazy val app = appProvider.get()
          val lang              = play.api.i18n.Lang.defaultLang
          implicit val m        = play.api.i18n.Messages.Implicits.applicationMessages(lang, app)
          Future {
            BadRequest(formWithErrors.errorsAsJson)
          }
        },
        success => {
          signUpUserUseCase.execute(Message(success._1, success._2, success._3, success._4)).map {
            httpErrorHandler.handle(_) { user =>
              Created.addingToSession(UserKeys.NameKey -> user.name.value, UserKeys.IdKey -> user.id.toString)
            }
          }
        }
      )
  }

  def userSignUpForm = Form(
    tuple(
      "username"   -> text,
      "email"      -> email,
      "passwd"     -> nonEmptyText(minLength = 2, maxLength = 50),
      "passwdConf" -> text
    ).verifying("Passwords do not coincide", fields => fields._3 == fields._4)
  )

}
