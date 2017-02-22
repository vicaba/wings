package controllers

import javax.inject.Singleton

import play.api.mvc.{Action, AnyContent, Controller}

@Singleton
class Index extends Controller {

  def apply: Action[AnyContent] = Action { r =>
    Ok(views.html.index("Your new application is ready."))
  }

}
