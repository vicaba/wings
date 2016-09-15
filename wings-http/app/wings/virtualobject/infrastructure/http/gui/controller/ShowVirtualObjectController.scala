package wings.virtualobject.infrastructure.http.gui.controller

import com.google.inject.Singleton
import play.api.mvc.{Action, Controller}

@Singleton
class ShowVirtualObjectController
 extends Controller {

  def apply(id: String) = Action {
    Ok(views.html.virtualobject.showvirtualobject.showvirtualobject(id))
  }

}
