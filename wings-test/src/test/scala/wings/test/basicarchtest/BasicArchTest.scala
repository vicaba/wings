package wings.test.basicarchtest

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
import wings.actor.mqtt.{MqttConnection, MqttTopics}
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
import wings.model.virtual.virtualobject.services.db.mongo.VirtualObjectMongoService
import wings.model.virtual.virtualobject.{VO, VOIdentityManager}
import wings.test.helper.database.MongoEnvironment
import wings.test.prebuilt.{Http, WebSocket}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class BasicArchTest
  extends FreeSpec
    with ScalaFutures
    with TestKitBase
    with ImplicitSender {

  implicit lazy val system = ActorSystem("testSystem", ConfigFactory.load("app"))

  implicit val fakeApp = FakeApplication()

  implicit val normalPatience = PatienceConfig(timeout = 1000 milliseconds)

  val extraPatience = PatienceConfig(timeout = 10 seconds)

  implicit val defaultTimeout = Timeout(1 second)

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

      val watchMqtt = VoWatch(path = MqttGlobals.voId)

      val watchMqtt2 = VoWatch(path = MqttGlobals2.voId)

      val sensedValue = SensedValue(voId = WebSocketGlobals.voIdUUID, value = "on")

      val actuateOverMqtt = VoActuate(path = MqttGlobals.voId, stateId = "on")

    }
  }

  object MqttGlobals {

    var testActor: ActorRef = null
    val voId = "0958232f-93c0-4559-9752-a362da8e07d3"
    val voIdUUID = UUIDHelper.tryFromString(voId).get

    val broker = "tcp://192.168.33.10:1883"

    val generalConfigInTopic = MqttTopics.provisionalConfigInTopic(voId)
    val generalConfigOutTopic = MqttTopics.provisionalConfigOutTopic(voId)

    val configInTopic = MqttTopics.configInTopic(voId)
    val configOutTopic = MqttTopics.configOutTopic(voId)

    val dataInTopic = MqttTopics.dataInTopic(voId)
    val dataOutTopic = MqttTopics.dataOutTopic(voId)

    object Messages {
      val metadata = VOMessage(
        voIdUUID,
        None,
        None,
        voId,
        Some(SenseCapability("power", "W")),
        Some(ActuateCapability("light", Array(ActuateState("on"), ActuateState("off"))))
      )

      val watchWebSocket = VoWatch(path = WebSocketGlobals.voId)

      val sensedValue = SensedValue(voId = voIdUUID, value = "5", unit = Some("C"))

      val actuateOverWebSocket = VoActuate(path = WebSocketGlobals.voId, stateId = "off")
    }

  }

  object MqttGlobals2 {

    val voId = "45c0ad7e-609e-429a-962a-a42ab1114584"
    val voIdUUID = UUIDHelper.tryFromString("45c0ad7e-609e-429a-962a-a42ab1114584").get

    val generalConfigInTopic = MqttTopics.provisionalConfigInTopic(voId)
    val generalConfigOutTopic = MqttTopics.provisionalConfigOutTopic(voId)

    val configInTopic = MqttTopics.configInTopic(voId)
    val configOutTopic = MqttTopics.configOutTopic(voId)

    val dataInTopic = MqttTopics.dataInTopic(voId)
    val dataOutTopic = MqttTopics.dataOutTopic(voId)

    object Messages {

      val metadata = VOMessage(
        voIdUUID,
        Some(MqttGlobals.voIdUUID),
        None,
        voId,
        Some(SenseCapability("status", "state")),
        Some(ActuateCapability("power", Array(ActuateState("on"), ActuateState("off"))))
      )

      val sensedValue = SensedValue(voId = voIdUUID, value = "5", unit = Some("C"))

    }
  }

  object HttpGlobals {
    var serverResponse: WSResponse = null
  }


  info("cleaning databases")
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


  "After 0.8 second the Virtual Object (HTTP) metadata is saved into the database" in {

    Thread.sleep(1000)

    val virtualObjectService = new VirtualObjectMongoService(MongoEnvironment.db1)(VOIdentityManager)

    val virtualObjectQuery = virtualObjectService.findOneByCriteria(
      Json.obj(VO.VOIDKey -> WebSocketGlobals.voId
      ))
    whenReady(virtualObjectQuery) {
      r =>
        r shouldBe defined
        r.get shouldBe a[VO]
    }
  }

  "An MQTT device should be able to establish a MQTT connection" in {
    val mqttConnection = MqttConnection(
      new MqttAsyncClient(MqttGlobals.broker, "A", new MemoryPersistence()), new MemoryPersistence(), new MqttConnectOptions()
    )
    val mqttActor = system.actorOf(MqttTestActor.props(MqttGlobals.broker, mqttConnection, self))
    MqttGlobals.testActor = mqttActor
  }

  "An MQTT device should be able to send a NameAcquisitionRequest" in {
    MqttGlobals.testActor ! Subscribe(MqttGlobals.generalConfigInTopic)
    expectMsg(MessageSent)

    MqttGlobals.testActor ! MqttTestActor.Messages.Publish(
      MqttGlobals.generalConfigOutTopic, Json.toJson(NameAcquisitionRequest(MqttGlobals.voId)).toString()
    )
    expectMsg(MessageSent)
    receiveOne(1 second) match {
      case m: MqttMessage => println(m)
      case _ => fail
    }

    MqttGlobals.testActor ! Subscribe(MqttGlobals.configInTopic)
    expectMsg(MessageSent)
    MqttGlobals.testActor ! Subscribe(MqttGlobals.dataInTopic)
    expectMsg(MessageSent)
  }

  "An MQTT device should be able to send it's metadata" in {
    Thread.sleep(800)
    MqttGlobals.testActor ! MqttTestActor.Messages.Publish(
      MqttGlobals.configOutTopic,
      Json.toJson(MqttGlobals.Messages.metadata).toString
    )
    expectMsg(MessageSent)
  }

  "After 1 second the Virtual Object (MQTT) metadata is saved into the database" in {

    Thread.sleep(1000)

    val virtualObjectService = new VirtualObjectMongoService(MongoEnvironment.db1)(VOIdentityManager)

    val virtualObjectQuery = virtualObjectService.findOneByCriteria(
      Json.obj(VO.VOIDKey -> MqttGlobals.voId
      ))
    whenReady(virtualObjectQuery) {
      r =>
        r shouldBe defined
        r.get shouldBe a[VO]
    }
  }

  "An MQTT2 device should be able to send it's metadata" in {
    Thread.sleep(800)
    MqttGlobals.testActor ! MqttTestActor.Messages.Publish(
      MqttGlobals.configOutTopic,
      Json.toJson(MqttGlobals2.Messages.metadata).toString
    )
    expectMsg(MessageSent)
  }

  "After 1 second the Virtual Object (MQTT2) metadata is saved into the database" in {

    Thread.sleep(1000)

    val virtualObjectService = new VirtualObjectMongoService(MongoEnvironment.db1)(VOIdentityManager)

    val virtualObjectQuery = virtualObjectService.findOneByCriteria(
      Json.obj(VO.VOIDKey -> MqttGlobals2.voId
      ))
    whenReady(virtualObjectQuery) {
      r =>
        r shouldBe defined
        r.get shouldBe a[VO]
    }
  }


  "Then watch MQTT messages from WebSocket and vice versa" in {
    MqttGlobals.testActor ! MqttTestActor.Messages.Publish(
      MqttGlobals.configOutTopic,
      Json.toJson(MqttGlobals.Messages.watchWebSocket).toString
    )
    expectMsg(MessageSent)

    WebSocketGlobals.testActor ! Send(
      Json.toJson(WebSocketGlobals.Messages.watchMqtt).toString)
    expectMsg(MessageSent)

    WebSocketGlobals.testActor ! Send(
      Json.toJson(WebSocketGlobals.Messages.watchMqtt2).toString)
    expectMsg(MessageSent)
  }

  "An MQTT device can send a sensed message" in {
    MqttGlobals.testActor ! MqttTestActor.Messages.Publish(
      MqttGlobals.dataOutTopic,
      Json.toJson(
        MqttGlobals.Messages.sensedValue
      ).toString
    )
    expectMsg(MessageSent)
  }

  "A WebSocket device can receive it (trust time)" in {
    val sensedValueFromWebSocket = receiveOne(Duration.Inf)
    Json.parse(sensedValueFromWebSocket.asInstanceOf[String]).validate[SensedValue].get shouldBe a[SensedValue]
  }

  "An MQTT2 device can send a sensed message" in {
    MqttGlobals.testActor ! MqttTestActor.Messages.Publish(
      MqttGlobals.dataOutTopic,
      Json.toJson(
        MqttGlobals2.Messages.sensedValue
      ).toString
    )
    expectMsg(MessageSent)
  }

  "A WebSocket device can receive it (from MQTT2, trust time)" in {
    val sensedValueFromWebSocket = receiveOne(Duration.Inf)
    Json.parse(sensedValueFromWebSocket.asInstanceOf[String]).validate[SensedValue].get shouldBe a[SensedValue]
  }

  "A WebSocket device can send a sensed message" in {
    WebSocketGlobals.testActor ! ActorJettyWebSocketAdapter.Messages.Send(
      Json.toJson(WebSocketGlobals.Messages.sensedValue).toString
    )
    expectMsg(MessageSent)
  }

  "An MQTT device can receive it (trust time)" in {
    val sensedValueFromMqtt = receiveOne(Duration.Inf)
    Json.parse(sensedValueFromMqtt.asInstanceOf[MqttMessage].payloadAsString()).validate[SensedValue].get shouldBe a[SensedValue]
  }

  "An MQTT device can send an actuate message to WebSocket device" in {
    MqttGlobals.testActor ! MqttTestActor.Messages.Publish(
      MqttGlobals.configOutTopic,
      Json.toJson(MqttGlobals.Messages.actuateOverWebSocket).toString
    )
    expectMsg(MessageSent)
  }

  "A WebSocket device can receive the actuate message" in {
    val actuatedValueFromWebSocket = receiveOne(Duration.Inf)
    Json.parse(actuatedValueFromWebSocket.asInstanceOf[String]).validate[VoActuate].get shouldBe a[VoActuate]
  }

  "A WebSocket device can send an actuate message to an MQTT device" in {
    WebSocketGlobals.testActor ! ActorJettyWebSocketAdapter.Messages.Send(
      Json.toJson(WebSocketGlobals.Messages.actuateOverMqtt).toString()
    )
    expectMsg(MessageSent)
  }

  "An MQTT device can receive the actuate message" in {
    val actuatedValueFromMqtt = receiveOne(Duration.Inf)
    Json.parse(actuatedValueFromMqtt.asInstanceOf[MqttMessage].payloadAsString()).validate[VoActuate].get shouldBe a[VoActuate]
  }

  "After 0.5 seconds the WebSocket device can disconnect" in {
    Thread.sleep(500)
    WebSocketGlobals.testActor ! GracefulShutdown
    expectMsg(MessageSent)
  }
}