package wings.actor.mqtt

import java.util.UUID

import akka.actor._
import akka.event.Logging
import org.eclipse.paho.client.mqttv3._
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import play.api.libs.json._
import wings.enrichments.UUIDHelper
import wings.enrichments.UUIDHelper._
import wings.actor.adapter.mqtt.paho
import wings.actor.adapter.mqtt.paho.{MqttMessage, ActorPahoMqttAdapter}
import wings.actor.adapter.mqtt.paho.PahoMqttAdapter._
import wings.actor.mqtt.{MqttTopics => Topics}

import wings.m2m.conf.model._
import wings.model.virtual.virtualobject.VOIdentityManager

import scala.collection.immutable.{HashMap, HashSet}
import scala.util.{Failure, Success, Try}

object MqttMaster {
  def props() = Props(MqttMaster())
}

case class MqttMaster() extends Actor with ActorPahoMqttAdapter {

  val logger = Logging(context.system, this)

  private val ConfigOutTopic = Topics.generalConfigOutTopic

  /**
   * This actor id
   */
  private val id = VOIdentityManager.next

  /**
   * Used virtual identities. For a key K in used identities, exists a Virtual Object V where V.id = K
   */
  private var usedIdentities = new HashSet[UUID]

  /**
   * This actor childs. @see usedIdentities
   */
  private var childs = new HashMap[UUID, ActorRef]

  // MQTT parameters
  protected val persistence = new MemoryPersistence()
  protected val connOpts = new MqttConnectOptions()
  protected val broker = "tcp://192.168.33.10:1883"

  protected var mqttAsyncClient: MqttAsyncClient = _

  def connectToMqttBroker(): Unit = {
    mqttAsyncClient = new MqttAsyncClient(broker, id.toBase64, persistence)
    connOpts.setCleanSession(true)

    mqttAsyncClient.connect(connOpts, null, new IMqttActionListener {

      override def onFailure(iMqttToken: IMqttToken, throwable: Throwable): Unit = {}

      override def onSuccess(iMqttToken: IMqttToken): Unit = {
        mqttAsyncClient.subscribe(Topics.generalConfigOutTopic, 2)
        logger.debug("Subscribed to: {}", Topics.generalConfigOutTopic)
      }
    })
    mqttAsyncClient.setCallback(this)
  }

  override def preStart(): Unit = connectToMqttBroker()

  def receive = {
    case msg @ MqttMessage(topic, payload, qos, retained, duplicate) =>
      topic match {
        case Topics.ConfigOutTopicPattern(id) => onConfigOutTopic(msg)
      }
    case _ => logger.debug("Mqtt Master has received an unknown message")
  }

  override def connectionLost(throwable: Throwable): Unit = {}

  override def deliveryComplete(token: IMqttDeliveryToken): Unit = {}

  def onConfigOutTopic(mqttMsg: paho.MqttMessage) = {

    Try(Json.parse(mqttMsg.payloadAsString())).map { msg =>
      msg.validate[Config].map {
        case NameAcquisitionRequest(v) =>
          logger.debug("MqttMaster has received a NameAcquisitionRequest")
          // Create VirtualIdentity
          UUIDHelper.tryFromString(v) match {
            case Success(identity) =>
              usedIdentities contains identity match {
                case true =>
                  logger.debug("MqttMaster has detected that identity already exists: {}", identity)
                  sendMqttMessage(mqttAsyncClient, rejectMessage(v))
                case false =>
                  logger.debug("MqttMaster creates proxy actor")
                  val actor = context.actorOf(MqttActor.props(identity.copy, broker))
                  registerVirtualObject(identity, actor)
                  sendMqttMessage(mqttAsyncClient, ackMessage(v))
              }
            case Failure(e) =>
              logger.debug(s"MqttMaster has failed to convert identity to an UUID, error message: {}", e)
              sendMqttMessage(mqttAsyncClient, rejectMessage(v))
          }
      }
    }
  }

  private def sendMqttMessage(mqttAsyncClient: MqttAsyncClient, message: MqttMessage) =
    mqttAsyncClient.publish(message.topic, message)

  private def rejectMessage(v: String) = MqttMessage(Topics.provisionalConfigInTopic(v), Json.toJson[Config]
    (NameAcquisitionReject("")).toString().getBytes, 2, false, false)

  private def ackMessage(v: String) = MqttMessage(Topics.provisionalConfigInTopic(v), Json.toJson[Config]
    (NameAcquisitionAck("")).toString().getBytes, 2, false, false)

  private def registerVirtualObject(id: UUID, ref: ActorRef): Unit = {
    usedIdentities += id
    childs += (id -> ref)
  }

}