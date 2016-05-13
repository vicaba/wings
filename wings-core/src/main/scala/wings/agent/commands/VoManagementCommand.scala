package wings.agent.commands


sealed trait VoManagementCommand

case class CreateVo(voId: String) extends VoManagementCommand
case class RemoveVo(voId: String) extends VoManagementCommand
