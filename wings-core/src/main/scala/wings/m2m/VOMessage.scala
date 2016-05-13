package wings.m2m

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json._
import wings.model.virtual.virtualobject.VO
import wings.model.virtual.virtualobject.actuate.ActuateCapability
import wings.model.virtual.virtualobject.sense.SenseCapability

case class VOMessage(voId: UUID,
                     pVoId: Option[UUID] = None,
                     children: Option[Array[String]] = None,
                     path: String,
                     senseCapability: Option[SenseCapability] = None,
                     actuateCapability: Option[ActuateCapability] = None
                   )

object VOMessage {
  val sensedVoReads: Reads[VOMessage] = (
      (__ \ VO.VOIDKey).read[UUID] and
      (__ \ VO.PVOIDKey).readNullable[UUID] and
      (__ \ VO.ChildrenKey).readNullable[Array[String]] and
      (__ \ VO.PathKey).read[String] and
      (__ \ VO.SenseCapabilityKey).readNullable[SenseCapability] and
      (__ \ VO.ActuateCapabilityKey).readNullable[ActuateCapability]
    )(VOMessage.apply _)

  val sensedVoWrites: OWrites[VOMessage] = (
      (__ \ VO.VOIDKey).write[UUID] and
      (__ \ VO.PVOIDKey).writeNullable[UUID] and
      (__ \ VO.ChildrenKey).writeNullable[Array[String]] and
      (__ \ VO.PathKey).write[String] and
      (__ \ VO.SenseCapabilityKey).writeNullable[SenseCapability] and
      (__ \ VO.ActuateCapabilityKey).writeNullable[ActuateCapability]
    )(unlift(VOMessage.unapply _))

  implicit val vOFormat = OFormat(sensedVoReads, sensedVoWrites)
}
