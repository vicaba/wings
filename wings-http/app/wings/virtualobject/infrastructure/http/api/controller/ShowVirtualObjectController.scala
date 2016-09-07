package wings.virtualobject.infrastructure.http.api.controller

import com.google.inject.Singleton
import httpplay.config.DependencyInjector._
import httpplay.error.HttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import scaldi.Injectable._
import wings.virtualobject.application.usecase.ShowVirtualObject
import wings.virtualobject.infrastructure.serialization.json.Implicits._

import scala.concurrent.ExecutionContext.Implicits.global


/**
  * ShowVirtualObject controller
  */
@Singleton
class ShowVirtualObjectController
  extends Controller {

  val showVirtualObjectUseCase: ShowVirtualObject.UseCase = inject[ShowVirtualObject.UseCase](identified by 'ShowVirtualObjectUseCase)
  val httpErrorHandler: HttpErrorHandler = inject[HttpErrorHandler](identified by 'HttpErrorHandler)

  def apply(id: String) = Action.async {
    showVirtualObjectUseCase.execute(ShowVirtualObject.Message(id)).map {
      httpErrorHandler.handle(_) {
        case Some(virtualObject) => Ok(Json.toJson(virtualObject));
        case None => NotFound
      }
    } recover { case t: Throwable => InternalServerError }
  }

}
