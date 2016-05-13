package controllers.virtualobject

import common.request.AuthenticatedAction
import database.mongodb.MongoEnvironment
import play.api.libs.iteratee.Enumeratee
import play.api.libs.json.{JsObject, JsArray, Json}
import play.api.mvc.Controller
import wings.model.virtual.virtualobject.sense.SenseCapability
import wings.model.virtual.virtualobject.{VO, VOIdentityManager}
import wings.model.virtual.virtualobject.services.db.mongo.VirtualObjectMongoService

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

  def searchStreamAPI = AuthenticatedAction.async(parse.json) {
    request =>
      request.body.validate[VOFinder].asOpt match {
        case None => Future {
          BadRequest
        }
        case Some(finder) =>
          val virtualObjectService = new VirtualObjectMongoService(MongoEnvironment.db1)(VOIdentityManager)
          val query = Json.obj(
            "$or" -> JsArray(Seq(
              Json.obj(s"${VO.SenseCapabilityKey}.${SenseCapability.NameKey}" -> finder.path),
              Json.obj(s"${VO.PathKey}" -> Json.obj("$regex" -> finder.path))
            )))
          val toResponse: Enumeratee[VO, JsObject] = Enumeratee.map[VO] {
            vo =>
              println("Stream Received")
              Json.obj(
                VO.VOIDKey -> vo.voId, VO.PathKey -> vo.path,
                VO.SenseCapabilityKey -> vo.senseCapability,
                VO.ActuateCapabilityKey -> vo.actuateCapability
              )
          }
          val stream = virtualObjectService.findStreamByCriteria(query).&>(toResponse)
          Future {
            Ok.chunked(stream)
          }
      }
  }

  def searchStreamTestAPI = AuthenticatedAction.async {
    request =>
      val virtualObjectService = new VirtualObjectMongoService(MongoEnvironment.db1)(VOIdentityManager)
      val query = Json.obj()
      val toResponse: Enumeratee[VO, JsObject] = Enumeratee.map[VO] {
        vo =>
          println("Stream Received")
          Json.obj(
            VO.VOIDKey -> vo.voId, VO.PathKey -> vo.path,
            VO.SenseCapabilityKey -> vo.senseCapability,
            VO.ActuateCapabilityKey -> vo.actuateCapability
          )
      }
      val stream = virtualObjectService.findStreamByCriteria(query).&>(toResponse)
      Future {
        Ok.chunked(stream)
      }
  }

  /**
    * API method to search for VirtualOBjects
    * @return
    */
  def searchAPI = AuthenticatedAction.async(parse.json) {
    request =>
      request.body.validate[VOFinder].asOpt match {
        case None => Future {
          BadRequest
        }
        case Some(finder) =>
          val virtualObjectService = new VirtualObjectMongoService(MongoEnvironment.db1)(VOIdentityManager)
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
    val virtualObjectService = new VirtualObjectMongoService(MongoEnvironment.db1)(VOIdentityManager)
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
