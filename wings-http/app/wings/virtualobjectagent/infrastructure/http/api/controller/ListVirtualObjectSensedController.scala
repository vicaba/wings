package wings.virtualobjectagent.infrastructure.http.api.controller

import scala.concurrent.ExecutionContext.Implicits._

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}

import wings.toolkit.db.ClauseValues.SortOrder.Ascendant
import wings.virtualobjectagent.application.usecase.ListVirtualObjectSensed
import wings.virtualobjectagent.application.usecase.ListVirtualObjectSensed.Message
import wings.virtualobjectagent.infrastructure.messages.serialization.json.Implicits._

import com.google.inject.Singleton
import httpplay.config.DependencyInjector._
import httpplay.error.HttpErrorHandler
import scaldi.Injectable._


/**
  * ListVirtualObjectSensedController
  */
@Singleton
class ListVirtualObjectSensedController extends Controller {

  val listVirtualObjectSensedUseCase: ListVirtualObjectSensed.UseCase =
    inject[ListVirtualObjectSensed.UseCase](identified by 'ListVirtualObjectSensedUseCase)

  val httpErrorHandler: HttpErrorHandler = inject[HttpErrorHandler](identified by 'HttpErrorHandler)

  def apply(id: String): Action[AnyContent] = Action.async { implicit req =>
    listVirtualObjectSensedUseCase.execute(Message(id, Some(Ascendant), None, None)).map {
      httpErrorHandler.handle(_)(l => Ok(Json.toJson(l)))
    }
  }

}
