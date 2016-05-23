package wings.client.actor.websocket

import akka.actor.Actor
import org.eclipse.jetty.websocket.api.{Session, WebSocketListener}
import wings.client.actor.websocket.ActorJettyWebSocketAdapter.Messages._

object ActorJettyWebSocketAdapter {
  object Messages {
    case class Binary(payload: Array[Byte], offset: Int, len: Int)
    case class Text(message: String)
    case class Connect(session: Session)
    case class Error(cause: Throwable)
    case class Close(statusCode: Int, reason: String)

    case class Send(text: String)
  }
}

trait ActorJettyWebSocketAdapter extends Actor with WebSocketListener {

  override def onWebSocketBinary(payload: Array[Byte], offset: Int, len: Int): Unit = {
    self ! Binary(payload, offset, len)
  }

  override def onWebSocketText(message: String): Unit = {
    self ! Text(message)
  }

  override def onWebSocketConnect(session: Session): Unit = {
    self ! Connect(session)
  }

  override def onWebSocketError(cause: Throwable): Unit = {
    self ! Error(cause)
  }

  override def onWebSocketClose(statusCode: Int, reason: String): Unit = {
    self ! Close(statusCode, reason)
  }
}
