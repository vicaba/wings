package wings.model.virtual.virtualobject

import play.api.libs.json._
import scala.reflect.runtime.universe.TypeTag
import scala.util.Try

trait AuthenticationCredentials

object AuthenticationCredentials {

  object Type extends Enumeration {

    type Type = Value

    val Basic = Value("basic")
  }

  val CredentialsTypeKey = "credentials"

  object AuthenticationCredentialsReads extends Reads[AuthenticationCredentials] {
    override def reads(json: JsValue): JsResult[AuthenticationCredentials] = json match {
      case json: JsObject =>
        Try(Type.withName((json \ "type").as[String])).toOption map {
            case Type.Basic => BasicAuthenticationCredentials.basicAuthenticationCredentialsFormat.reads(json)
            case _ => JsError("")
        } getOrElse JsError("")
      case _ => JsError("")
    }
  }
}


case class BasicAuthenticationCredentials(name: String) extends AuthenticationCredentials

object BasicAuthenticationCredentials {
  implicit val basicAuthenticationCredentialsFormat = Json.format[BasicAuthenticationCredentials]
}

/**
  * There is no need for typeTags, the content is of type T, match on content
  *
  */
case class Authenticated[+T](credentials: AuthenticationCredentials, content: T)

object Authenticated {
  object AuthenticatedReads extends Reads[Authenticated[JsValue]] {
    override def reads(json: JsValue): JsResult[Authenticated[JsObject]] = json match {
      case json: JsObject =>
        (json \ AuthenticationCredentials.CredentialsTypeKey).asOpt[JsValue].map {
          jsAuth =>
            AuthenticationCredentials.AuthenticationCredentialsReads.reads(jsAuth).flatMap {
              auth =>
                JsSuccess(Authenticated(auth, json - AuthenticationCredentials.CredentialsTypeKey))
            }
        }.getOrElse(JsError(""))
      case _ => JsError("")
    }
  }

  object AuthenticatedWrites extends OWrites[Authenticated[_]] {
    override def writes(o: Authenticated[_]): JsObject = {
      val typeTag = implicitly[TypeTag[Any]]
      Json.obj()
    }
  }
}

object Main  {
  def main(args: Array[String]) {
    Authenticated.AuthenticatedWrites.writes(Authenticated(BasicAuthenticationCredentials("a"), "a"))
  }
}
