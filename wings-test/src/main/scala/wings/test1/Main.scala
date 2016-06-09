package wings.test1

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestProbe
import org.eclipse.paho.client.mqttv3.{MqttAsyncClient, MqttConnectOptions}
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import wings.actor.mqtt.MqttConnection
import wings.client.actor.mqtt.MqttTestActor
import wings.client.actor.mqtt.MqttTestActor.Messages.Subscribe
import wings.client.actor.websocket.ActorJettyWebSocketAdapter
import wings.enrichments.UUIDHelper
import wings.m2m.VOMessage
import wings.m2m.conf.model.NameAcquisitionRequest
import wings.model.virtual.virtualobject.actuate.{ActuateCapability, ActuateState}
import wings.model.virtual.virtualobject.sense.SenseCapability
import wings.model.virtual.virtualobject.sensed.SensedValue
import wings.enrichments.UUIDHelper._
import wings.model.virtual.operations.VoWatch
import wings.test.prebuilt.{Http, WebSocket}
import akka.testkit.TestProbe


import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


object Main {

  object MqttGlobals {

    val broker = "tcp://192.168.33.10:1883"

    def generalConfigInTopic(id: UUID) = s"$id/i/config/in"

    def generalConfigOutTopic(id: UUID) = s"$id/i/config/out"

    def configInTopic(id: UUID) = s"$id/config/in"

    def configOutTopic(id: UUID) = s"$id/config/out"

    def dataInTopic(id: UUID) = s"$id/data/in"

    def dataOutTopic(id: UUID) = s"$id/data/out"

    object Messages {

      def metadata(id: UUID) = VOMessage(
        id,
        None,
        None,
        id.toString,
        Some(SenseCapability("power", "W")),
        Some(ActuateCapability("light", Array(ActuateState("on"), ActuateState("off"))))
      )

      def sensedValue(id: UUID) = SensedValue(voId = id, value = "5", unit = Some("C"))

    }

  }

  object WebSocketGlobals {

    val voId = "73f86a2e-1004-4011-8a8f-3f78cdd6113c"
    val voIdUUID = UUIDHelper.tryFromString("73f86a2e-1004-4011-8a8f-3f78cdd6113c").get

    object Messages {
      val metadata = VOMessage(
        voIdUUID,
        None,
        None,
        voId,
        Some(SenseCapability("status", "state")),
        Some(ActuateCapability("running/stopped", Array(ActuateState("on"))))
      )

      def watch(path: String) = VoWatch(path = path)

    }

  }

  def main(args: Array[String]) {

    import wings.test.database.mongodb._

    cleanMongoDatabase

    implicit val system = ActorSystem("system-test1")

    val numberOfSenders = 10

    val uuidList = 0 until numberOfSenders map (_ => UUID.randomUUID())
    val actorList = uuidList.map { uuid =>

      val mqttConnection = MqttConnection(
        new MqttAsyncClient(MqttGlobals.broker, uuid.toBase64, new MemoryPersistence()), new MemoryPersistence(), new MqttConnectOptions()
      )
      (uuid, system.actorOf(MqttTestActor.props(MqttGlobals.broker, mqttConnection, TestProbe().ref)))

    }

    actorList.foreach { case (uuid, actorRef) =>

      actorRef ! Subscribe(MqttGlobals.generalConfigInTopic(uuid))
      actorRef ! MqttTestActor.Messages.Publish(
        MqttGlobals.generalConfigOutTopic(uuid), Json.toJson(NameAcquisitionRequest(uuid.toString)).toString()
      )

    }

    Thread.sleep(5000)

    actorList.foreach { case (uuid, actorRef) =>

      actorRef ! MqttTestActor.Messages.Publish.apply(
        MqttGlobals.configOutTopic(uuid),
        Json.toJson(MqttGlobals.Messages.metadata(uuid))
      )

    }

    println("Metadata Sent")

    // Request a WebSocket connection and watch sensed messages

    val receiverProbe = TestProbe()(system)

    val userRegisteredResponse: WSResponse = Await.result(Http.Request.userRegistration.execute(), 300.seconds)

    val webSocketActor = WebSocket.getConnection(userRegisteredResponse, receiverProbe.ref)(system)

    webSocketActor ! ActorJettyWebSocketAdapter.Messages.Send(
      Json.toJson(NameAcquisitionRequest(WebSocketGlobals.voId)).toString()
    )

    Thread.sleep(500)

    webSocketActor ! ActorJettyWebSocketAdapter.Messages.Send(
      Json.toJson(WebSocketGlobals.Messages.metadata).toString
    )

    Thread.sleep(500)

    actorList.foreach { case (uuid, actorRef) =>

      webSocketActor ! ActorJettyWebSocketAdapter.Messages.Send(
        Json.toJson(WebSocketGlobals.Messages.watch(uuid.toString)).toString
      )

    }

    // Schedule MQTT clients to send sensed messages every period of time

    actorList.foreach { case (uuid, actorRef) =>

      system.scheduler.schedule(5 seconds, 2 seconds, actorRef,
        MqttTestActor.Messages.Publish.apply(
          MqttGlobals.dataOutTopic(uuid),
          Json.toJson(MqttGlobals.Messages.sensedValue(uuid))))
    }

    println("HERE")
    assert(receiverProbe.receiveN(numberOfSenders, 6 seconds).length == numberOfSenders)


  }


}