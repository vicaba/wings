package wings.actor.websocket

import java.util.UUID

import akka.actor._
import models.user.WebUser
import wings.actor.util.ActorUtilities
import wings.agent.{ArchitectureDriver, CoreAgent}

object WebSocketActor {

  def props(virtualObjectId: UUID, user: WebUser)(out: ActorRef) = Props(new WebSocketActor(virtualObjectId, user, out))

}

/**
  * Actor to handle WebSocket connections
  *
  * @param out an actor reference representing the other side of the connection, usually the browser client
  */
class WebSocketActor(val virtualObjectId: UUID, user: WebUser, out: ActorRef)
  extends Actor
    with CoreAgent
  with ActorUtilities {

  override val toDeviceProps: Props = WebSocketDriver.props(virtualObjectId, out, self)

  override val toArchitectureProps: Props = ArchitectureDriver.props(virtualObjectId, self)
}
