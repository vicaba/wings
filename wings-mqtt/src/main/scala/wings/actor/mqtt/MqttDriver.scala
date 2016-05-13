package wings.actor.mqtt

import java.util.UUID

import akka.actor.{Props, ActorRef, Actor}
import org.eclipse.paho.client.mqttv3._
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import play.api.libs.json.{JsResult, JsValue, Json}
import wings.actor.adapter.mqtt.paho.{MqttMessage, ActorPahoMqttAdapter}
import wings.actor.pipeline.MsgEnv
import wings.agent.DeviceDriver
import wings.enrichments.UUIDHelper.UUIDEnrichment
import wings.actor.adapter.mqtt.paho.PahoMqttAdapter._
import wings.m2m.VOMessage
import wings.m2m.conf.model.Config
import wings.model.virtual.operations.{VoOp, VoActuate}
import wings.model.virtual.virtualobject.sensed.SensedValue

case class MqttConnection(client: IMqttAsyncClient, persistence: MqttClientPersistence, connOpts: MqttConnectOptions)

object MqttDriver {
  def props(virtualObjectId: UUID, broker: String, continuation: ActorRef) = Props(MqttDriver(virtualObjectId, broker, continuation))
}

case class MqttDriver(virtualObjectId: UUID, broker: String, continuation: ActorRef)
  extends Actor
    with DeviceDriver
    with ActorPahoMqttAdapter {

  import context._

  override type DeviceConnectionContext = MqttConnection

  override type DeviceMessageType = MqttMessage

  /**
    * Gets the dataOut topic
    *
    * @return
    */
  protected val DataOutTopic: String = virtualObjectId.toString + "/data/out"


  protected val DataInTopic: String = virtualObjectId.toString + "/data/in"


  /**
    * Gets the configIn topic
    *
    * @return
    */
  protected val ConfigInTopic: String = virtualObjectId.toString + "/config/in"

  /**
    * Gets the configOutTopic
    *
    * @return
    */
  protected val ConfigOutTopic: String = virtualObjectId.toString + "/config/out"

  override def preStart(): Unit = {
    connectToBroker()
  }

  override def receive: Actor.Receive = {
    case a: Any => println(a)
  }

  override def deliveryComplete(token: IMqttDeliveryToken): Unit = {}

  override def connectionLost(throwable: Throwable): Unit = {}

  def connectToBroker() = {
    logger.debug(s"MqttDriver with id ${virtualObjectId} connecting to broker")
    // Create the MQTT connection
    val mqttConnection = MqttConnection(
      new MqttAsyncClient(broker, virtualObjectId.toBase64, new MemoryPersistence()), new MemoryPersistence(), new MqttConnectOptions()
    )
    mqttConnection.connOpts.setCleanSession(true)
    // Set Paho connection callbacks
    mqttConnection.client.setCallback(this)
    // Connect to the server
    mqttConnection.client.connect(mqttConnection.connOpts, null, new IMqttActionListener {

      override def onFailure(iMqttToken: IMqttToken, throwable: Throwable): Unit = {}

      override def onSuccess(iMqttToken: IMqttToken): Unit = {
        // Subscribe to configOutTopic
        logger.debug(s"MqttDriver with id ${virtualObjectId} subscribing to $ConfigOutTopic")
        mqttConnection.client.subscribe(ConfigOutTopic, 2)
        logger.debug(s"MqttDriver with id ${virtualObjectId} subscribing to $DataOutTopic")
        mqttConnection.client.subscribe(DataOutTopic, 2)
        // become normal state
        become(driverState(mqttConnection, continuation))
      }
    })
  }

  def validateJsonForConfig(json: JsValue): JsResult[Any] = {
    json.validate[Config] orElse json.validate[VOMessage] orElse json.validate[VoOp]
  }

  def validateJsonForData(json: JsValue): JsResult[SensedValue] = {
    json.validate[SensedValue]
  }

  override def toDeviceReceive(dc: MqttConnection): PartialFunction[Any, Unit] = {
    case voActuate: VoActuate =>
      val json = Json.toJson(voActuate).toString()
      val msg = MqttMessage(ConfigInTopic, json.getBytes, 2, false, false)
      dc.client.publish(ConfigInTopic, msg)
    case sv: SensedValue =>
      val json = Json.toJson(sv).toString()
      val msg = MqttMessage(DataInTopic, json.getBytes, 2, false, false)
      dc.client.publish(DataInTopic, msg)
  }

  override def toArchitectureReceive(dc: MqttConnection, continuation: ActorRef): PartialFunction[DeviceMessageType, Unit] = {
    case msg: MqttMessage =>
      logger.debug(s"MQTT message received at topic ${msg.topic}")
      val parsedMsg = msgToJson(msg)
      msg.topic match {
        case ConfigOutTopic =>
          logger.debug("Forwarding message to architecture")
          parsedMsg map(validateJsonForConfig(_) map{continuation ! MsgEnv.ToArch(_)})
        case DataOutTopic =>
          logger.debug("Forwarding message to architecture")
          parsedMsg map(validateJsonForData(_) map(continuation ! MsgEnv.ToArch(_)))
        case _ => println("Cannot understand message")
      }
  }
}
