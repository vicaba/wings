package controllers

import javax.inject.Singleton

import play.api.mvc.{Action, Controller}

@Singleton
class Index extends Controller {

  def apply = Action { r =>
    Ok(views.html.index("Your new application is ready."))
  }

}
