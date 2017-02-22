package wings.virtualobject.infrastructure.http.gui.controller

import scala.concurrent.ExecutionContext.Implicits.global

import play.api.mvc.{Action, AnyContent, Controller}

import wings.virtualobject.application.usecase.ListVirtualObject

import com.google.inject.Singleton
import httpplay.config.DependencyInjector._
import httpplay.error.HttpErrorHandler
import scaldi.Injectable._


/**
  * ListVirtualObjectController
  */
@Singleton
class ListVirtualObjectController extends Controller {

  val listVirtualObjectUseCase: ListVirtualObject.UseCase =
    inject[ListVirtualObject.UseCase](identified by 'ListVirtualObjectUseCase)
  val httpErrorHandler: HttpErrorHandler = inject[HttpErrorHandler](identified by 'HttpErrorHandler)

  def apply(): Action[AnyContent] = Action.async {
    listVirtualObjectUseCase.execute(ListVirtualObject.Message(None, None)).map {
      httpErrorHandler.handle(_)(l => Ok(views.html.virtualobject.listvirtualobject.listvirtualobject(l)))
    }
  }

}
