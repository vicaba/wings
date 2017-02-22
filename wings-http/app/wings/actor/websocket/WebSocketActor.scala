package wings.actor.websocket

import java.util.UUID

import akka.actor._

import wings.actor.util.ActorUtilities
import wings.virtualobjectagent.domain.agent.{ArchitectureDriver, CoreAgent}

object WebSocketActor {

  def props(virtualObjectId: UUID)(out: ActorRef): Props = Props(new WebSocketActor(virtualObjectId, out))

}

/**
  * Actor to handle WebSocket connections
  *
  * @param out an actor reference representing the other side of the connection, usually the browser client
  */
class WebSocketActor(val virtualObjectId: UUID, out: ActorRef) extends Actor with CoreAgent with ActorUtilities {

  override val toDeviceProps: Props = WebSocketDriver.props(virtualObjectId, out, self)

  override val toArchitectureProps: Props = ArchitectureDriver.props(virtualObjectId, self)
}
