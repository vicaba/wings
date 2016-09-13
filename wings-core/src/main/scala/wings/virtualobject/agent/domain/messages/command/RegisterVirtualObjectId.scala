package wings.virtualobject.agent.domain.messages.command

import wings.virtualobject.domain.VirtualObject


sealed trait RegisterVirtualObjectId

case class NameAcquisitionRequest(virtualObjectId: VirtualObject.IdType) extends RegisterVirtualObjectId

case class NameAcquisitionAck(virtualObjectId: VirtualObject.IdType) extends RegisterVirtualObjectId

case class NameAcquisitionReject(virtualObjectId: VirtualObject.IdType) extends RegisterVirtualObjectId