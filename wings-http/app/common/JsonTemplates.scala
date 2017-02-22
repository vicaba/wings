package common

import play.api.libs.json.{Json, JsValue}


object JsonTemplates {

  def singleMsg(msg: String): JsValue = Json.obj("msg" -> msg)

}
