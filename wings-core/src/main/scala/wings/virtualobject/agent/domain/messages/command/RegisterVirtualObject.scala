package wings.virtualobject.agent.domain.messages.command

import wings.virtualobject.domain.VirtualObject


sealed trait RegisterVirtualObject

case class NameAcquisitionRequest(virtualObjectId: VirtualObject.IdType) extends RegisterVirtualObject

case class NameAcquisitionAck(virtualObjectId: VirtualObject.IdType) extends RegisterVirtualObject

case class NameAcquisitionReject(virtualObjectId: VirtualObject.IdType) extends RegisterVirtualObject