package wings.virtualobject.domain.messages.command

sealed trait OperateOnVirtualObject

case class ActuateOnVirtualObject(path: String, stateId: String) extends OperateOnVirtualObject

case class WatchVirtualObject(path: String) extends OperateOnVirtualObject