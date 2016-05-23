package wings.test1

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestProbe
import org.eclipse.paho.client.mqttv3.{MqttAsyncClient, MqttConnectOptions}
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import play.api.libs.json.Json
import wings.actor.mqtt.MqttConnection
import wings.client.actor.mqtt.MqttTestActor
import wings.client.actor.mqtt.MqttTestActor.Messages.Subscribe
import wings.enrichments.UUIDHelper
import wings.m2m.VOMessage
import wings.m2m.conf.model.NameAcquisitionRequest
import wings.model.virtual.virtualobject.actuate.{ActuateCapability, ActuateState}
import wings.model.virtual.virtualobject.sense.SenseCapability
import wings.model.virtual.virtualobject.sensed.SensedValue
import wings.enrichments.UUIDHelper._
import wings.model.virtual.operations.{VoActuate, VoWatch}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


object Main {

  object Mqtt {

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

  object WebSocket {

    val voId = "73f86a2e-1004-4011-8a8f-3f78cdd6113c"
    val voIdUUID = UUIDHelper.tryFromString("73f86a2e-1004-4011-8a8f-3f78cdd6113c").get

    val webSocketUrl = "ws://localhost:9000/api/admin/ws/socket/"

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

    implicit val system = ActorSystem("system-test1")

    val uuidList = 0 until 10 map (_ => UUID.randomUUID())
    val actorList = uuidList.map { uuid =>

      val mqttConnection = MqttConnection(
        new MqttAsyncClient(Mqtt.broker, uuid.toBase64, new MemoryPersistence()), new MemoryPersistence(), new MqttConnectOptions()
      )
      (uuid, system.actorOf(MqttTestActor.props(Mqtt.broker, mqttConnection, TestProbe().ref)))

    }

    actorList.foreach { case (uuid, actorRef) =>

      println(uuid)

      actorRef ! Subscribe(Mqtt.generalConfigInTopic(uuid))
      actorRef ! MqttTestActor.Messages.Publish(
        Mqtt.generalConfigOutTopic(uuid), Json.toJson(NameAcquisitionRequest(uuid.toString)).toString()
      )

    }

    Thread.sleep(1000)

    actorList.foreach { case (uuid, actorRef) =>

      println("Sending Metadata")

        actorRef ! MqttTestActor.Messages.Publish.apply(
          Mqtt.configOutTopic(uuid),
          Json.toJson(Mqtt.Messages.metadata(uuid))
        )

    }

    actorList.foreach { case (uuid, actorRef) =>

      system.scheduler.schedule(20 seconds, 10 seconds, actorRef,
        MqttTestActor.Messages.Publish.apply(
          Mqtt.dataOutTopic(uuid),
        Json.toJson(Mqtt.Messages.sensedValue(uuid))))

    }

    println("Done")

  }

}