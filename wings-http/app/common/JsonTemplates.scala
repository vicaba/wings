package common

import play.api.libs.json.Json

/**
  * Created by vicaba on 27/10/15.
  */
object JsonTemplates {

  def singleMsg(msg: String) = Json.obj("msg" -> msg)

}
