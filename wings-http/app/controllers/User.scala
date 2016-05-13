package controllers


import common.JsonTemplates
import database.mongodb.MongoEnvironment
import models.user.services.db.mongo.UserMongoService
import models.user.{User => UserM}
import models.user.UserIdentityManager
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object User {

  def userSignInForm = Form(
    tuple(
      "username" -> text,
      "email" -> email, // change to email
      "passwd" -> nonEmptyText(minLength = 2, maxLength = 50)
    )
  )

  def userSignUpForm = Form(
    tuple(
      "username" -> text,
      "email" -> email, // change to email
      "passwd" -> nonEmptyText(minLength = 2, maxLength = 50),
      "passwdConf" -> text
    ).verifying("Passwords do not coincide", fields => fields._3 == fields._4)
  )
}


/**
  * The user controller
  */
class User
  extends Controller {

  def signInAPI = Action.async(parse.json) {
    implicit request =>
      User.userSignInForm.bindFromRequest().fold(
        formWithErrors => {
          Future {
            BadRequest(formWithErrors.errorsAsJson)
          }
        },
        success => {
          val userService = new UserMongoService(MongoEnvironment.db1)(UserIdentityManager)
          userService.findOneByCriteria(Json.obj(UserM.emailKey -> success._2, UserM.usernameKey -> success._1, UserM.passwordKey -> success._3)) map {
            case Some(user) =>
              Created.addingToSession(UserIdentityManager.name -> user.id.get.toString, UserM.usernameKey -> user.username)
            case None =>
              Forbidden(JsonTemplates.singleMsg("User, email and password do not match"))
          }
        }
      )
  }

  def signUpAPI = Action.async(parse.json) {
    implicit request =>
      println(request.body)
      User.userSignUpForm.bindFromRequest().fold(
        formWithErrors => {
          Future {
            BadRequest(formWithErrors.errorsAsJson)
          }
        },
        success => {
          val identity = UserIdentityManager.next
          val userService = new UserMongoService(MongoEnvironment.db1)(UserIdentityManager)
          userService.findOneByCriteria(Json.obj(models.user.User.usernameKey -> success._1)).flatMap {
            _.fold
            {
              userService.create(models.user.WebUser(Some(identity), None, success._1, success._2, success._3)).map {
                // TODO: Send back a url with the created resource URI (for example the user profile)
                case Right(user) => Created.addingToSession(UserM.usernameKey -> user.username, UserIdentityManager.name -> identity.toString)
                case Left(wr) => BadRequest(JsonTemplates.singleMsg(wr.message))
              }.recover {
                case e: Throwable => InternalServerError(JsonTemplates.singleMsg(e.getMessage))
              }
            }
            {
              webUser =>Future { BadRequest(JsonTemplates.singleMsg("User already exists!")) }
            }
          }


        }
      )
  }

  def logoutAPI = Action.async(parse.anyContent) {
    implicit request =>
      Future { Ok.withNewSession }
  }

}
