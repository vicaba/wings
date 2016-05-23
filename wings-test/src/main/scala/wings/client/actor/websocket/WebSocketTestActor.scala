package wings.client.actor.websocket

import java.net.URI

import akka.actor.{ActorRef, Actor, Props, Stash}
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.client.{ClientUpgradeRequest, WebSocketClient}
import wings.client.actor.ActorTestMessages.{GracefulShutdown, MessageSent}
import wings.client.actor.websocket.ActorJettyWebSocketAdapter.Messages._

import scala.util.Try

object WebSocketTestActor {
  def props(client: WebSocketClient, serverUri: URI, wsRequest: ClientUpgradeRequest, testSender: ActorRef): Props = Props(WebSocketTestActor(client, serverUri, wsRequest, testSender))
}

case class WebSocketTestActor(client: WebSocketClient, serverUri: URI, wsRequest: ClientUpgradeRequest, testSender: ActorRef)
  extends Actor with Stash with ActorJettyWebSocketAdapter {

  import context._

  override def preStart() = {
    client.connect(this, serverUri, wsRequest)
  }

  override def receive: Receive = {
    case Connect(session) =>
      unstashAll()
      become(connectedState(session))
    case msg => stash()
  }

  def connectedState(session: Session): Receive = {
    case Send(text) =>
      Try(session.getRemote.sendString(text)) recover { case t: Throwable => println(t) }
      sender ! MessageSent
    case GracefulShutdown =>
      deallocateWebsocketResources(session)
      sender ! MessageSent
    case Text(text) => testSender ! text
    case _ =>
  }

  private def deallocateWebsocketResources(session: Session): Unit = {
    Try(session.close()) recover { case t: Throwable => println(t) }
  }

}