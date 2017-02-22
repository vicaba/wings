package wings.virtualobject.infrastructure.http.gui.controller

import com.google.inject.Singleton
import play.api.mvc.{Action, Controller}

@Singleton
class MapVirtualObjectController extends Controller {

  def apply() = Action {
    Ok(views.html.virtualobject.mapvirtualobject.mapvirtualobject())
  }

}
