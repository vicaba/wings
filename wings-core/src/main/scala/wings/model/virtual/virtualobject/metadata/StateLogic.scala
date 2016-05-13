package wings.model.virtual.virtualobject.metadata

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class StateLogic(someLogic: String)

object StateLogic {

  object StateLogicReads extends Reads[StateLogic] {
    override def reads(json: JsValue): JsResult[StateLogic] = json match {
      case json: JsObject =>
        (json \ "someLogic").asOpt[String].map {
          someLogic => JsSuccess(StateLogic(someLogic))
        } getOrElse JsError("Key mismatch")
      case _ => JsError("Not an object")
    }
  }

  object StateLogicWrites extends OWrites[StateLogic] {
    override def writes(o: StateLogic): JsObject = Json.obj("someLogic" -> o.someLogic)
  }

  implicit val stateLogicFormat = OFormat(StateLogicReads, StateLogicWrites)

}