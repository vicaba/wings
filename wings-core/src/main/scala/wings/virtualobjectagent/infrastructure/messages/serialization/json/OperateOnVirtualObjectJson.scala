package wings.virtualobjectagent.infrastructure.messages.serialization.json

import play.api.libs.functional.syntax._
import play.api.libs.json.{OWrites, Reads, _}
import play.api.libs.json.Reads._

import wings.virtualobjectagent.domain.messages.command.{ActuateOnVirtualObject, WatchVirtualObject}
import wings.virtualobjectagent.infrastructure.messages.keys.OperateOnVirtualObjectKeys

object OperateOnVirtualObjectJson {

  val WatchVirtualObjectOperationValue     = "vo/watch"
  val ActuateOnVirtualObjectOperationValue = "vo/actuate"

  val WatchVirtualObjectReads: Reads[WatchVirtualObject] =
    ((__ \ OperateOnVirtualObjectKeys.OperationKey).read[String].filter(_ == WatchVirtualObjectOperationValue) ~>
      (__ \ OperateOnVirtualObjectKeys.PathKey).read[String]).map { path =>
      WatchVirtualObject(path)
    }

  object WatchVirtualObjectWrites extends OWrites[WatchVirtualObject] {

    override def writes(o: WatchVirtualObject): JsObject =
      Json.obj(
        OperateOnVirtualObjectKeys.OperationKey -> WatchVirtualObjectOperationValue,
        OperateOnVirtualObjectKeys.PathKey      -> o.path
      )

  }

  val WatchVirtualObjectFormat = OFormat(WatchVirtualObjectReads, WatchVirtualObjectWrites)

  val ActuateOnVirtualObjectReads: Reads[ActuateOnVirtualObject] =
    ((__ \ OperateOnVirtualObjectKeys.OperationKey).read[String].filter(_ == ActuateOnVirtualObjectOperationValue) ~>
      (__ \ OperateOnVirtualObjectKeys.PathKey).read[String] ~
      (__ \ OperateOnVirtualObjectKeys.StateIdKey).read[String]).tupled.map {
      case (a, b) =>
        ActuateOnVirtualObject(a, b)
    }

  object ActuateOnVirtualObjectWrites extends OWrites[ActuateOnVirtualObject] {

    override def writes(o: ActuateOnVirtualObject): JsObject =
      Json.obj(
        OperateOnVirtualObjectKeys.OperationKey -> ActuateOnVirtualObjectOperationValue,
        OperateOnVirtualObjectKeys.PathKey      -> o.path,
        OperateOnVirtualObjectKeys.StateIdKey   -> o.stateId
      )

  }

  val ActuateOnVirtualObjectFormat = OFormat(ActuateOnVirtualObjectReads, ActuateOnVirtualObjectWrites)

}
