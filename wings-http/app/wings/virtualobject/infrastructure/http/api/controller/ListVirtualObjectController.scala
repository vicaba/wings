package wings.virtualobject.infrastructure.http.api.controller

import com.google.inject.Singleton
import httpplay.config.DependencyInjector._
import httpplay.error.HttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import scaldi.Injectable._
import wings.virtualobject.application.usecase.ListVirtualObject
import wings.virtualobject.infrastructure.serialization.json.Implicits._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * ListVirtualObjectController
  */
@Singleton
class ListVirtualObjectController
  extends Controller {

  val listVirtualObjectUseCase: ListVirtualObject.UseCase = inject[ListVirtualObject.UseCase](identified by 'ListVirtualObjectUseCase)

  val httpErrorHandler: HttpErrorHandler = inject[HttpErrorHandler](identified by 'HttpErrorHandler)

  def apply() = Action.async {
    listVirtualObjectUseCase.execute(ListVirtualObject.Message(None, None)).map {
      httpErrorHandler.handle(_)(l => Ok(Json.toJson(l)))
    }
  }

}
