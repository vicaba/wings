package wings.virtualobject.agent.domain.messages.event

import java.util.UUID

sealed trait VirtualObjectOperated

case class VirtualObjectSensed
(
  id: UUID = UUID.randomUUID(),
  voId: UUID,
  value: String,
  unit: Option[String] = None,
  stateId: Option[String] = None
)
  extends VirtualObjectOperated

case class VirtualObjectActuated
(
  id: UUID = UUID.randomUUID(),
  voId: UUID,
  stateId: String
)
  extends VirtualObjectOperated
