package wings.virtualobject.infrastructure.http.gui.controller

import play.api.mvc.{Action, AnyContent, Controller}

import com.google.inject.Singleton


@Singleton
class ShowVirtualObjectController extends Controller {

  def apply(id: String): Action[AnyContent] = Action {
    Ok(views.html.virtualobject.showvirtualobject.showvirtualobject(id))
  }

}
