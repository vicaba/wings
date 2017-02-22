package wings.virtualobject.infrastructure.http.gui.controller

import play.api.mvc.{Action, AnyContent, Controller}

import com.google.inject.Singleton


@Singleton
class MapVirtualObjectController extends Controller {

  def apply(): Action[AnyContent] = Action {
    Ok(views.html.virtualobject.mapvirtualobject.mapvirtualobject())
  }

}
