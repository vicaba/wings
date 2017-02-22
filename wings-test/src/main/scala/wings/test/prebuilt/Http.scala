package wings.test.prebuilt

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.json.Json
import play.api.libs.ws.ahc.AhcWSClient
import play.api.libs.ws.WSRequest

import scala.concurrent.ExecutionContext.Implicits.global

object Http {

  lazy val httpClient: AhcWSClient = AhcWSClient()(ActorMaterializer()(ActorSystem()))

  val playSessionKey = "PLAY_SESSION"

  object User {
    val username = "david"
    val email    = "d@d.d"
    val password = "davidvernet"
  }

  object Request {

    def userRegistration: WSRequest = {
      val jsonBody = Json.obj(
        "username"   -> User.username,
        "email"      -> User.email,
        "passwd"     -> User.password,
        "passwdConf" -> User.password
      )
      val request =
        httpClient
          .url("http://127.0.0.1:9000/api/v1/users")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(jsonBody)
          .withMethod("POST")
      request
    }

  }
}
