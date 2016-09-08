package wings.virtualobject.domain

import java.time.ZonedDateTime
import java.util.UUID

import wings.model.{HasIdentity, HasVoId}
import wings.model.virtual.virtualobject.actuate.ActuateCapability

case class VirtualObject
(
  override val id: Option[UUID],
  override val voId: UUID,
  pVoId: Option[UUID],
  actorRef: Option[String],
  children: Option[Array[String]],
  path: String,
  metadata: Option[UUID],
  creationTime: ZonedDateTime,
  deletionTime: Option[ZonedDateTime],
  senseCapability: Option[SenseCapability],
  actuateCapability: Option[ActuateCapability]
)
  extends HasIdentity[UUID] with HasVoId
