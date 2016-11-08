package wings.test1

import java.net.URI
import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSResponse
import scaldi.Injectable._
import wings.actor.mqtt.router.MqttRouter
import wings.client.actor.mqtt.MqttTestActor2
import wings.client.actor.mqtt.MqttTestActor2.Messages.Subscribe
import wings.client.actor.websocket.ActorJettyWebSocketAdapter
import wings.config.DependencyInjector._
import wings.enrichments.UUIDHelper
import wings.test.prebuilt.{Http, WebSocket}
import wings.test.util.RandomPointGenerator
import wings.test.util.RandomPointGenerator.Point
import wings.virtualobjectagent.domain.messages.command.{NameAcquisitionRequest, VirtualObjectBasicDefinition, WatchVirtualObject}
import wings.virtualobjectagent.domain.messages.event.VirtualObjectSensed
import wings.virtualobject.domain.{ActuateCapability, ActuateState, SenseCapability}
import wings.virtualobjectagent.infrastructure.messages.serialization.json.Implicits._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


object Main {

  object MqttGlobals {

    def generalConfigInTopic(id: UUID) = s"$id/i/config/in"

    def generalConfigOutTopic(id: UUID) = s"$id/i/config/out"

    def configInTopic(id: UUID) = s"$id/config/in"

    def configOutTopic(id: UUID) = s"$id/config/out"

    def dataInTopic(id: UUID) = s"$id/data/in"

    def dataOutTopic(id: UUID) = s"$id/data/out"

    object Messages {

      def metadata(id: UUID) = VirtualObjectBasicDefinition(
        id,
        None,
        None,
        Some(id.toString),
        Json.obj() ++ RandomPointGenerator.generateRandomJsonPoint(),
        Some(SenseCapability("power", "W")),
        Some(ActuateCapability("light", Array(ActuateState("on"), ActuateState("off"))))
      )

      def sensedValue(id: UUID) = VirtualObjectSensed(voId = id, value = "5", unit = Some("C"))

    }

  }

  object WebSocketGlobals {

    val voId = "73f86a2e-1004-4011-8a8f-3f78cdd6113c"
    val voIdUUID = UUIDHelper.tryFromString("73f86a2e-1004-4011-8a8f-3f78cdd6113c").get

    object Messages {
      val metadata = VirtualObjectBasicDefinition(
        voIdUUID,
        None,
        None,
        Some(voId),
        Json.obj(),
        Some(SenseCapability("status", "state")),
        Some(ActuateCapability("running/stopped", Array(ActuateState("on"))))
      )

      def watch(path: String) = WatchVirtualObject(path = path)

    }

  }

  def main(args: Array[String]) {

    import wings.test.database.mongodb._

    cleanMongoDatabase

    implicit val system = ActorSystem("system-test1")

    val numberOfSenders = 200

    val uuidList = 0 until numberOfSenders map (_ => UUID.randomUUID())
    val router = system.actorOf(MqttRouter.props(inject[URI](identified by 'MqttBroker).toString))
    val actorList = uuidList.map { uuid =>

      Thread.sleep(20)

      (uuid, system.actorOf(MqttTestActor2.props(router, TestProbe().ref)))

    }

    actorList.foreach { case (uuid, actorRef) =>

      Thread.sleep(5)

      actorRef ! Subscribe(MqttGlobals.generalConfigInTopic(uuid))
      actorRef ! MqttTestActor2.Messages.Publish(
        MqttGlobals.generalConfigOutTopic(uuid), Json.toJson(NameAcquisitionRequest(uuid)).toString()
      )

    }

    actorList.foreach { case (uuid, actorRef) =>

      Thread.sleep(20)

      actorRef ! MqttTestActor2.Messages.Publish(
        MqttGlobals.configOutTopic(uuid),
        Json.toJson(MqttGlobals.Messages.metadata(uuid))
      )

    }

    println("Metadata Sent")

    // Request a WebSocket connection and watch sensed messages

    val receiverProbe = TestProbe()(system)

    val userRegisteredResponse: WSResponse = Await.result(Http.Request.userRegistration.execute(), 300.seconds)

    val webSocketActor = WebSocket.getConnection(inject[URI](identified by 'WebSocketServerWithPath), userRegisteredResponse, receiverProbe.ref)(system)

    webSocketActor ! ActorJettyWebSocketAdapter.Messages.Send(
      Json.toJson(NameAcquisitionRequest(WebSocketGlobals.voIdUUID)).toString()
    )

    Thread.sleep(300)

    webSocketActor ! ActorJettyWebSocketAdapter.Messages.Send(
      Json.toJson(WebSocketGlobals.Messages.metadata).toString
    )

    Thread.sleep(300)

    println("WebSocket has sent metadata")

    actorList.foreach { case (uuid, actorRef) =>

      webSocketActor ! ActorJettyWebSocketAdapter.Messages.Send(
        Json.toJson(WebSocketGlobals.Messages.watch(uuid.toString)).toString
      )

    }

    // Schedule MQTT clients to send sensed messages every period of time
    println("Starting to schedule")

    actorList.foreach { case (uuid, actorRef) =>

      system.scheduler.scheduleOnce(5 seconds, actorRef,
        MqttTestActor2.Messages.Publish.apply(
          MqttGlobals.dataOutTopic(uuid),
          Json.toJson(MqttGlobals.Messages.sensedValue(uuid))))
    }

    println("Done")
    assert(receiverProbe.receiveN(numberOfSenders + 20, 9 seconds).length == numberOfSenders)


  }


}