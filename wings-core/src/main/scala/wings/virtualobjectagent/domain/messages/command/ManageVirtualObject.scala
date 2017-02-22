package wings.virtualobjectagent.domain.messages.command

import java.util.UUID

sealed trait ManageVirtualObject

case class CreateVirtualObject(voId: UUID) extends ManageVirtualObject

case class RemoveVirtualObject(voId: UUID) extends ManageVirtualObject
