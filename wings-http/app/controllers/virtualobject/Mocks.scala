package controllers.virtualobject

import java.time.ZonedDateTime
import java.util.UUID

import play.api.mvc.{Action, Controller}
import wings.model.virtual.virtualobject.{VO, VOIdentityManager}
import wings.model.virtual.virtualobject.services.db.mongo.VirtualObjectMongoService
import wings.config.DependencyInjector._
import scaldi.Injectable._
import wings.services.db.MongoEnvironment
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by vicaba on 28/10/15.
  */
class Mocks extends Controller {

  val mongoEnv: MongoEnvironment = inject[MongoEnvironment](identified by 'MongoEnvironment)

  def generateVOs = Action {
    val virtualObjectService = new VirtualObjectMongoService(mongoEnv.mainDb)(VOIdentityManager)
    for (i <- 0 to 1000) {
      val thisVo = VO(Some(UUID.randomUUID()), UUID.randomUUID(), None, None, None, s"temperature",
        None, ZonedDateTime.now(), None, None, None)
      // Add a VirtualObject
      virtualObjectService.create(thisVo)
    }
    Ok
  }

}
