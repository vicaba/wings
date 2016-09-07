package wings.test.websockettest

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKitBase}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.eclipse.paho.client.mqttv3.{MqttAsyncClient, MqttConnectOptions}
import org.scalatest.FreeSpec
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.ws._
import play.api.test.FakeApplication
import reactivemongo.api.commands.WriteResult
import wings.actor.adapter.mqtt.paho.MqttMessage
import wings.actor.mqtt.MqttConnection
import wings.client.actor.ActorTestMessages.{GracefulShutdown, MessageSent}
import wings.client.actor.mqtt.MqttTestActor
import wings.client.actor.mqtt.MqttTestActor.Messages.Subscribe
import wings.client.actor.websocket.ActorJettyWebSocketAdapter.Messages.Send
import wings.client.actor.websocket.{ActorJettyWebSocketAdapter, WebSocketTestActor}
import wings.enrichments.UUIDHelper
import wings.m2m.VOMessage
import wings.m2m.conf.model.NameAcquisitionRequest
import wings.model.virtual.operations.{VoActuate, VoWatch}
import wings.model.virtual.virtualobject.actuate.{ActuateCapability, ActuateState}
import wings.model.virtual.virtualobject.sense.SenseCapability
import wings.model.virtual.virtualobject.sensed.SensedValue
import wings.virtualobject.infrastructure.repository.mongodb.VirtualObjectMongoRepository
import wings.model.virtual.virtualobject.{VO, VOIdentityManager}
// import wings.test.helper.database.MongoEnvironment
import wings.test.prebuilt.{Http, WebSocket}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class WebSocketTest
  extends FreeSpec
  with ScalaFutures
  with TestKitBase
  with ImplicitSender {


  object WebSocketGlobals {

    var testActor: ActorRef = null
    val voId = "73f86a2e-1004-4011-8a8f-3f78cdd6113c"
    val voIdUUID = UUIDHelper.tryFromString(voId).get

    val webSocketUrl = "ws://localhost:9000/api/admin/ws/socket/"

    object Messages {
      val metadata = VOMessage(
        voIdUUID,
        None,
        None,
        WebSocketGlobals.voId,
        Some(SenseCapability("status", "state")),
        Some(ActuateCapability("running/stopped", Array(ActuateState("on"))))
      )

      val watchItself = VoWatch(path = voId)

    }

  }

  object HttpGlobals {
    var serverResponse: WSResponse = _
  }

  implicit lazy val system = ActorSystem("testSystem", ConfigFactory.load("app"))

  implicit val fakeApp = FakeApplication()

  implicit val normalPatience = PatienceConfig(timeout = 1000 milliseconds)

  val extraPatience = PatienceConfig(timeout = 10 seconds)

  val f = wings.test.database.mongodb.cleanMongoDatabase
  whenReady(f) {
    case wr: WriteResult => assert(wr.ok === true)
    case _ => fail()
  }

  "A User should be able to register receiving a 201 Http status" in {
    // Maybe the HTTP application isn't compiled yet, give the test an extraPatience
    val futureResponse = Http.Request.userRegistration.execute()
    whenReady(futureResponse) {
      response =>
        HttpGlobals.serverResponse = response
        assert(response.status == Status.CREATED)
    }(extraPatience)
  }

  s"The response should have a cookie with name ${Http.playSessionKey}" in {
    HttpGlobals.serverResponse.cookie(Http.playSessionKey) shouldBe defined
  }

  "The User (an HTTP device) should be able to establish a WebSocket connection" in {
    WebSocketGlobals.testActor = WebSocket.getConnection(HttpGlobals.serverResponse, self)(system)
  }

  "The HTTP device should be able to send a NameAcquisitionRequest" in {
    WebSocketGlobals.testActor ! ActorJettyWebSocketAdapter.Messages.Send(
      Json.toJson(NameAcquisitionRequest(WebSocketGlobals.voId)).toString()
    )
    expectMsg(MessageSent)
    receiveOne(Duration.Inf)
  }

  "The HTTP device should be able to send it's metadata" in {
    val uuid = UUIDHelper.tryFromString(WebSocketGlobals.voId).get
    WebSocketGlobals.testActor ! ActorJettyWebSocketAdapter.Messages.Send(
      Json.toJson(WebSocketGlobals.Messages.metadata).toString
    )
    expectMsg(MessageSent)
  }

  "Then watch MQTT messages from WebSocket and vice versa" in {
    WebSocketGlobals.testActor ! Send(
      Json.toJson(WebSocketGlobals.Messages.watchItself).toString)
    expectMsg(MessageSent)
  }

  "After 0.5 seconds the WebSocket device can disconnect" in {
    Thread.sleep(500)
    WebSocketGlobals.testActor ! GracefulShutdown
    expectMsg(MessageSent)
  }

}
