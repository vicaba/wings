package wings.virtualobject.infrastructure.http.api.controller

import scala.concurrent.ExecutionContext.Implicits.global

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}

import wings.virtualobject.application.usecase.ShowVirtualObject
import wings.virtualobject.infrastructure.serialization.json.Implicits._

import com.google.inject.Singleton
import httpplay.config.DependencyInjector._
import httpplay.error.HttpErrorHandler
import scaldi.Injectable._

/**
  * ShowVirtualObject controller
  */
@Singleton
class ShowVirtualObjectController extends Controller {

  val showVirtualObjectUseCase: ShowVirtualObject.UseCase =
    inject[ShowVirtualObject.UseCase](identified by 'ShowVirtualObjectUseCase)

  val httpErrorHandler: HttpErrorHandler = inject[HttpErrorHandler](identified by 'HttpErrorHandler)

  def apply(id: String): Action[AnyContent] = Action.async {
    showVirtualObjectUseCase.execute(ShowVirtualObject.Message(id)).map {
      httpErrorHandler.handle(_) {
        case Some(virtualObject) => Ok(Json.toJson(virtualObject));
        case None                => NotFound
      }
    } recover { case t: Throwable => InternalServerError }
  }

}
