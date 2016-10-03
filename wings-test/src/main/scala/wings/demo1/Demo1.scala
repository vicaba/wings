package wings.demo1

import java.net.URI
import java.util.UUID

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorSystem, Props}
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

    val r = scala.util.Random

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

      def sensedValue(id: UUID) = VirtualObjectSensed(voId = id, value = r.nextInt(50).toString, unit = Some("C"))

    }

  }

  def main(args: Array[String]) {

    import wings.test.database.mongodb._

    cleanMongoDatabase

    implicit val system = ActorSystem("system-test1")

    val numberOfSenders = 50

    val uuidList = 0 until numberOfSenders map (_ => UUID.randomUUID())
    val router = system.actorOf(MqttRouter.props(inject[URI](identified by 'MqttBroker).toString))
    val actorList = uuidList.map { uuid =>

      Thread.sleep(50)

      (uuid, system.actorOf(MqttTestActor2.props(router, TestProbe().ref)))

    }

    actorList.foreach { case (uuid, actorRef) =>

      Thread.sleep(100)

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



    // Schedule MQTT clients to send sensed messages every period of time
    println("Starting to schedule")


    actorList.foreach { case (uuid, actorRef) =>

      system.scheduler.schedule(1 second, 2 seconds) {
        actorRef ! MqttTestActor2.Messages.Publish.apply(
          MqttGlobals.dataOutTopic(uuid),
          Json.toJson(MqttGlobals.Messages.sensedValue(uuid)))
      }
    }

  }


}