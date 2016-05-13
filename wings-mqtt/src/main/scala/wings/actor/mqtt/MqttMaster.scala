package wings.actor.mqtt

import java.util.UUID

import akka.actor._
import akka.event.Logging
import akka.remote.RemoteScope
import org.eclipse.paho.client.mqttv3._
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import play.api.libs.json._
import wings.actor.cluster.pubsub.PSMediator
import wings.enrichments.UUIDHelper
import wings.enrichments.UUIDHelper._
import wings.actor.adapter.mqtt.paho
import wings.actor.adapter.mqtt.paho.{MqttMessage, ActorPahoMqttAdapter}
import wings.actor.adapter.mqtt.paho.PahoMqttAdapter._

import wings.m2m.conf.model._
import wings.model.virtual.virtualobject.VOIdentityManager
import wings.utils.test.{RandomActor, SuperRandomActor}

import scala.collection.immutable.{HashMap, HashSet}
import scala.util.{Failure, Success, Try}

object MqttMaster {
  def props() = Props(MqttMaster())
}

case class MqttMaster() extends Actor with ActorPahoMqttAdapter {

  val logger = Logging(context.system, this)

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

  protected def configOutTopic = "+/i/config/out"

  protected def configOutTopic(id: String) = s"$id/i/config/out"

  protected def configInTopic = "+/i/config/in"

  protected def configInTopic(id: String) = s"$id/i/config/in"

  protected var mqttAsyncClient: MqttAsyncClient = new MqttAsyncClient(broker, id.toBase64, persistence)

  override def preStart(): Unit = {

    mqttAsyncClient = new MqttAsyncClient(broker, id.toBase64, persistence)
    connOpts.setCleanSession(true)

    mqttAsyncClient.connect(connOpts, null, new IMqttActionListener {

      override def onFailure(iMqttToken: IMqttToken, throwable: Throwable): Unit = {}

      override def onSuccess(iMqttToken: IMqttToken): Unit = {
        mqttAsyncClient.subscribe(configOutTopic, 2)
        logger.debug(s"Subscribed to: $configOutTopic")
      }
    })
    mqttAsyncClient.setCallback(this)
  }

  def receive = {
    case msg @ MqttMessage(topic, payload, qos, retained, duplicate) =>
      println("message received")
      topic match {
        case configOutTopic =>
          onConfigOutTopic(msg)
      }
    case _ => println("nada")
  }

  override def connectionLost(throwable: Throwable): Unit = {}

  override def deliveryComplete(token: IMqttDeliveryToken): Unit = {}

  def onConfigOutTopic(mqttMsg: paho.MqttMessage) = {

    Try(Json.parse(mqttMsg.payloadAsString())).map { msg =>
      msg.validate[Config].map {
        case NameAcquisitionRequest(v) =>
          logger.debug("Received NameAcquisitionRequest")

          // Create VirtualIdentity
          UUIDHelper.tryFromString(v) match {
            case Success(identity) =>
              usedIdentities contains identity match {
                case true =>
                  // Send a Reject message
                  logger.debug("Contains identity")
                  val message =
                    MqttMessage(configInTopic(v), Json.toJson[Config](NameAcquisitionReject("")).toString().getBytes, 2, false, false)
                  mqttAsyncClient.publish(message.topic, message)
                case false =>
                  // Send an ACK message
                  logger.debug("Creating proxy actor")
                  val actor = context.actorOf(MqttActor.props(identity.copy, broker))
                  registerVirtualObject(identity, actor)
                  val message =
                    MqttMessage(configInTopic(v), Json.toJson[Config](NameAcquisitionAck("")).toString().getBytes, 2, false, false)
                  mqttAsyncClient.publish(message.topic, message)
              }
            case Failure(e) =>
              logger.debug(s"$e")
              val message =
                MqttMessage(configInTopic(v), Json.toJson[Config](NameAcquisitionReject("")).toString().getBytes, 2, false, false)
              mqttAsyncClient.publish(message.topic, message)
          }
      }
    }
  }

  def registerVirtualObject(id: UUID, ref: ActorRef): Unit = {
    usedIdentities += id
    childs += (id -> ref)
  }

}