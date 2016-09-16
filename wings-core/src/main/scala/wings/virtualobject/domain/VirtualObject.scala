package wings.virtualobject.domain

import java.util.UUID

import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json}
import wings.virtualobject.domain.VirtualObject.IdType

case class VirtualObject
(
  id: IdType,
  parentId: Option[IdType],
  children: Option[Array[String]],
  path: String,
  metadata: JsObject = Json.obj(),
  creationTime: DateTime,
  deletionTime: Option[DateTime],
  senseCapability: Option[SenseCapability],
  actuateCapability: Option[ActuateCapability]
)

object VirtualObject {

  type IdType = UUID

}