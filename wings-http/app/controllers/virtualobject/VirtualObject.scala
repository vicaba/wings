package controllers.virtualobject

import common.request.AuthenticatedAction
import play.api.libs.iteratee.Enumeratee
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.mvc.{Action, Controller}
import scaldi.Injectable._
import wings.config.DependencyInjector._
import wings.model.virtual.virtualobject.sense.SenseCapability
import wings.virtualobject.infrastructure.repository.mongodb.VirtualObjectMongoRepository
import wings.model.virtual.virtualobject.{VO, VOIdentityManager}
import wings.services.db.MongoEnvironment

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  *
  * @param path the word to find
  */
case class VOFinder(path: String)

/**
  *
  */
object VOFinder {
  implicit val voFinderFormat = Json.format[VOFinder]
}

/**
  * VirtualObject controller
  */
class VirtualObject extends Controller {

  val mongoEnv: MongoEnvironment = inject[MongoEnvironment](identified by 'MongoEnvironment)


  def apply = Action.async {
    Future(Ok)
  }


  /**
    * API method to search for VirtualObjects
    * @return
    */
  def searchAPI = AuthenticatedAction.async(parse.json) {
    request =>
      request.body.validate[VOFinder].asOpt match {
        case None => Future {
          BadRequest
        }
        case Some(finder) =>
          val virtualObjectService = VirtualObjectMongoRepository(mongoEnv.mainDb)(VOIdentityManager)
          val query = Json.obj(
            "$or" -> JsArray(Seq(
              Json.obj(s"${VO.SenseCapabilityKey}.${SenseCapability.NameKey}" -> finder.path),
              Json.obj(s"${VO.PathKey}" -> Json.obj("$regex" -> finder.path))
            )))
          virtualObjectService.findByCriteria(query).map {
            list =>
              Ok(
                Json.toJson(list.map {
                  vo =>
                    Json.obj(
                      VO.VOIDKey -> vo.voId, VO.PathKey -> vo.path,
                      VO.SenseCapabilityKey -> vo.senseCapability,
                      VO.ActuateCapabilityKey -> vo.actuateCapability
                    )
                })
              )
          }
      }
  }

  def searchTestAPI = AuthenticatedAction.async {
    val virtualObjectService = VirtualObjectMongoRepository(mongoEnv.mainDb)(VOIdentityManager)
    val query = Json.obj()
    virtualObjectService.findByCriteria(query).map {
      list =>
        Ok(
          Json.toJson(list.map {
            vo =>
              Json.obj(
                VO.VOIDKey -> vo.voId, VO.PathKey -> vo.path,
                VO.SenseCapabilityKey -> vo.senseCapability,
                VO.ActuateCapabilityKey -> vo.actuateCapability
              )
          })
        )
    }

  }
}
