package wings.test.prebuilt

import java.net.{HttpCookie, URI}

import akka.actor.{ActorRef, ActorSystem}
import org.eclipse.jetty.websocket.client.{ClientUpgradeRequest, WebSocketClient}
import play.api.libs.ws.{WSClient, WSResponse}
import wings.client.actor.websocket.WebSocketTestActor

import scala.collection.JavaConverters._


object WebSocket {

  val webSocketUrl = "ws://localhost:9000/api/v1/admin/ws/socket"

  def getConnection(response: WSResponse, testActor: ActorRef = ActorRef.noSender)(actorSystem: ActorSystem = ActorSystem())
  = {
    val webSocketServerUri = new URI(webSocketUrl)
    val webSocketClient = new WebSocketClient()
    val sessionCookie = response.cookie(Http.playSessionKey).get.value.get

    val webSocketRequest = new ClientUpgradeRequest()
    val httpCookie = new HttpCookie(Http.playSessionKey, sessionCookie)
    webSocketRequest.setCookies(List(httpCookie).asJava)
    webSocketClient.start()

    val realTestActor = if (testActor == ActorRef.noSender) actorSystem.deadLetters else testActor

    actorSystem.actorOf(WebSocketTestActor.props(webSocketClient, webSocketServerUri, webSocketRequest, realTestActor))

  }

}
