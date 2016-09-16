package wings.virtualobject.agent.domain.messages.event

import java.time.ZonedDateTime
import java.util.UUID

sealed trait VirtualObjectOperated

case class VirtualObjectSensed
(
  id: UUID = UUID.randomUUID(),
  voId: UUID,
  value: String,
  unit: Option[String] = None,
  creationDate: ZonedDateTime = ZonedDateTime.now()
)
  extends VirtualObjectOperated

case class VirtualObjectActuated
(
  id: UUID = UUID.randomUUID(),
  voId: UUID,
  stateId: String
)
  extends VirtualObjectOperated
