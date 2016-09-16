package wings.actor.mqtt

import java.util.UUID

import akka.actor._
import wings.actor.util.ActorUtilities
import wings.virtualobject.domain.agent.{ArchitectureDriver, CoreAgent}


object MqttActor {

  def props(virtualObjectId: UUID, conn: ActorRef): Props = Props(new MqttActor(virtualObjectId, conn))
}

case class MqttActor(virtualObjectId: UUID, conn: ActorRef)

  extends CoreAgent with ActorUtilities {

  override val toDeviceProps: Props = MqttDriver.props(virtualObjectId, conn, self)

  override val toArchitectureProps: Props = ArchitectureDriver.props(virtualObjectId, self)

}