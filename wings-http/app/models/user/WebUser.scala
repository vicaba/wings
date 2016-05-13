package models.user

import java.util.UUID

import play.api.libs.json._
import wings.model.{ActorReferenced, HasIdentity}


import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

trait User extends HasIdentity[UUID] {
  val id: Option[UUID]
  val username: String
  val email: String
  val password: String
}

object User {

  val usernameKey = "username"

  val emailKey = "email"

  val passwordKey = "password"

}

/**
 * Class containing only a WebUser (this user does not belong to any group)
 * @param username  the user name
 * @param email the user email
 * @param password  the user password
 */
case class WebUser(
                    override val id: Option[UUID],
                    virtualObjectId: Option[UUID],
                    override val username: String,
                    override val email: String,
                    override val password: String
                  )
  extends User

object WebUser {

  val virtualObjectIdKey = "virtualObjectId"

  def apply(u: User) = new WebUser(u.id, None, u.username, u.email, u.password)

  object WebUserReads extends Reads[WebUser] {
    override def reads(json: JsValue): JsResult[WebUser] = json match {
      case json: JsObject =>

        (for {
          username <- (json \ User.usernameKey).asOpt[String]
          email <- (json \ User.emailKey).asOpt[String]
          password <- (json \ User.passwordKey).asOpt[String]
        } yield {
            val virtualObjectId = (json \ virtualObjectIdKey).asOpt[UUID]
            val id = (json \ UserIdentityManager.name).asOpt[UUID]
            JsSuccess(new WebUser(id, virtualObjectId, username, email, password))
          }) getOrElse JsError("Can't convert to WebUser")
      case _ => JsError("Can't convert to WebUser")
    }
  }

  val webUserWrites: OWrites[WebUser] = (
    (__ \ UserIdentityManager.name).writeNullable[UUID] and
    (__ \ virtualObjectIdKey).writeNullable[UUID] and
    (__ \ User.usernameKey).write[String] and
    (__ \ User.emailKey).write[String] and
    (__ \ User.passwordKey).write[String]
    )(unlift(WebUser.unapply))

  implicit val webUserFormat = OFormat(WebUserReads, webUserWrites)

}