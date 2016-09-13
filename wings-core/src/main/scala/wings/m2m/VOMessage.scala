package wings.m2m

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json._
import wings.virtualobject.domain.{ActuateCapability, SenseCapability}
import wings.virtualobject.infrastructure.keys.VirtualObjectKeys
import wings.virtualobject.infrastructure.serialization.json.Implicits._

case class VOMessage(voId: UUID,
                     pVoId: Option[UUID] = None,
                     children: Option[Array[String]] = None,
                     path: String,
                     senseCapability: Option[SenseCapability] = None,
                     actuateCapability: Option[ActuateCapability] = None
                   )

object VOMessage {
  val sensedVoReads: Reads[VOMessage] = (
      (__ \ VirtualObjectKeys.IdKey).read[UUID] and
      (__ \ VirtualObjectKeys.PIdKey).readNullable[UUID] and
      (__ \ VirtualObjectKeys.ChildrenKey).readNullable[Array[String]] and
      (__ \ VirtualObjectKeys.PathKey).read[String] and
      (__ \ VirtualObjectKeys.SenseCapabilityKey).readNullable[SenseCapability] and
      (__ \ VirtualObjectKeys.ActuateCapabilityKey).readNullable[ActuateCapability]
    )(VOMessage.apply _)

  val sensedVoWrites: OWrites[VOMessage] = (
      (__ \ VirtualObjectKeys.IdKey).write[UUID] and
      (__ \ VirtualObjectKeys.PIdKey).writeNullable[UUID] and
      (__ \ VirtualObjectKeys.ChildrenKey).writeNullable[Array[String]] and
      (__ \ VirtualObjectKeys.PathKey).write[String] and
      (__ \ VirtualObjectKeys.SenseCapabilityKey).writeNullable[SenseCapability] and
      (__ \ VirtualObjectKeys.ActuateCapabilityKey).writeNullable[ActuateCapability]
    )(unlift(VOMessage.unapply _))

  implicit val vOFormat = OFormat(sensedVoReads, sensedVoWrites)
}
