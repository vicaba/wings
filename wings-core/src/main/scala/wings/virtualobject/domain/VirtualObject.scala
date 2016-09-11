package wings.virtualobject.domain

import java.time.ZonedDateTime
import java.util.UUID

import play.api.libs.json.{JsObject, Json}
import wings.virtualobject.domain.VirtualObject.IdType

case class VirtualObject
(
  id: IdType,
  parentId: Option[UUID],
  actorRef: Option[String],
  children: Option[Array[String]],
  path: String,
  metadata: JsObject = Json.obj(),
  creationTime: ZonedDateTime,
  deletionTime: Option[ZonedDateTime],
  senseCapability: Option[SenseCapability],
  actuateCapability: Option[ActuateCapability]
)

object VirtualObject {

  type IdType = UUID

}