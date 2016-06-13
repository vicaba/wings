package wings.actor.mqtt

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import org.eclipse.paho.client.mqttv3._
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import play.api.libs.json.{JsResult, JsValue, Json}
import wings.actor.adapter.mqtt.paho.{ActorPahoMqttAdapter, MqttMessage}
import wings.actor.pipeline.MsgEnv
import wings.agent.DeviceDriver
import wings.enrichments.UUIDHelper.UUIDEnrichment
import wings.actor.adapter.mqtt.paho.PahoMqttAdapter._
import wings.actor.mqtt.router.MqttRouter
import wings.m2m.VOMessage
import wings.m2m.conf.model.Config
import wings.model.virtual.operations.{VoActuate, VoOp}
import wings.model.virtual.virtualobject.sensed.SensedValue

case class MqttConnection(client: IMqttAsyncClient, persistence: MqttClientPersistence, connOpts: MqttConnectOptions)

object MqttDriver {
  def props(virtualObjectId: UUID, conn: ActorRef, continuation: ActorRef) = Props(MqttDriver(virtualObjectId, conn, continuation))
}

case class MqttDriver(virtualObjectId: UUID, conn: ActorRef, continuation: ActorRef)
  extends Actor
    with DeviceDriver
    with ActorPahoMqttAdapter {

  import context._

  override type DeviceConnectionContext = ActorRef

  override type DeviceMessageType = MqttMessage

  lazy val DataOutTopic: String = MqttTopics.dataOutTopic(virtualObjectId)

  lazy val DataInTopic: String = MqttTopics.dataInTopic(virtualObjectId)

  lazy val ConfigInTopic: String = MqttTopics.configInTopic(virtualObjectId)

  lazy val ConfigOutTopic: String = MqttTopics.configOutTopic(virtualObjectId)

  override def preStart(): Unit = {
    logger.debug(s"MqttDriver with id {} subscribing to {}", virtualObjectId, ConfigOutTopic)
    conn ! MqttRouter.Subscribe(ConfigOutTopic, self)
    logger.debug(s"MqttDriver with id {} subscribing to {}", virtualObjectId, DataOutTopic)
    conn ! MqttRouter.Subscribe(DataOutTopic, self)
    become(driverState(conn, continuation))
  }

  override def receive: Actor.Receive = {
    case a: Any => println(a)
  }

  override def deliveryComplete(token: IMqttDeliveryToken): Unit = {}

  override def connectionLost(throwable: Throwable): Unit = {}

  def validateJsonForConfig(json: JsValue): JsResult[Any] = {
    json.validate[Config] orElse json.validate[VOMessage] orElse json.validate[VoOp]
  }

  def validateJsonForData(json: JsValue): JsResult[SensedValue] = {
    json.validate[SensedValue]
  }

  override def toDeviceReceive(dc: ActorRef): PartialFunction[Any, Unit] = {
    case voActuate: VoActuate =>
      val json = Json.toJson(voActuate).toString()
      val msg = MqttMessage(ConfigInTopic, json.getBytes, 2, false, false)
      conn ! MqttRouter.Publish(msg)
    case sv: SensedValue =>
      val json = Json.toJson(sv).toString()
      val msg = MqttMessage(DataInTopic, json.getBytes, 2, false, false)
      conn ! MqttRouter.Publish(msg)
  }


  override def toArchitectureReceive(dc: ActorRef, continuation: ActorRef): PartialFunction[DeviceMessageType, Unit] = {
    case msg: MqttMessage =>
      logger.debug(s"MQTT message received at topic ${msg.topic}")
      val parsedMsg = msgToJson(msg)
      msg.topic match {
        case ConfigOutTopic =>
          logger.debug("Forwarding message to architecture")
          parsedMsg map (validateJsonForConfig(_) map {
            continuation ! MsgEnv.ToArch(_)
          })
        case DataOutTopic =>
          logger.debug("Forwarding message to architecture")
          parsedMsg map (validateJsonForData(_) map (continuation ! MsgEnv.ToArch(_)))
        case _ => println("Cannot understand message")
      }
  }
}
