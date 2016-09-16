package wings.virtualobject.domain.messages.command

import java.time.ZonedDateTime

import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json}
import wings.virtualobject.domain.{ActuateCapability, SenseCapability, VirtualObject}

trait ModifyVirtualObjectDefinition

case class VirtualObjectBasicDefinition
(
  id: VirtualObject.IdType,
  parentId: Option[VirtualObject.IdType],
  children: Option[Array[String]],
  path: Option[String],
  metadata: JsObject = Json.obj(),
  senseCapability: Option[SenseCapability],
  actuateCapability: Option[ActuateCapability]
)
extends ModifyVirtualObjectDefinition
{

  def toVirtualObject: VirtualObject =
    VirtualObject(
      id,
      parentId,
      children,
      path.getOrElse(id.toString),
      metadata,
      DateTime.now(),
      None,
      senseCapability,
      actuateCapability)

}




