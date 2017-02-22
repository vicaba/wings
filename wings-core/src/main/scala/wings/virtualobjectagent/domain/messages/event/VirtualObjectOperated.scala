package wings.virtualobjectagent.domain.messages.event

import java.util.UUID

import org.joda.time.DateTime

sealed trait VirtualObjectOperated

case class VirtualObjectSensed(
    id: UUID = UUID.randomUUID(),
    voId: UUID,
    value: String,
    unit: Option[String] = None,
    creationDate: DateTime = DateTime.now()
) extends VirtualObjectOperated

case class VirtualObjectActuated(
    id: UUID = UUID.randomUUID(),
    voId: UUID,
    stateId: String
) extends VirtualObjectOperated
