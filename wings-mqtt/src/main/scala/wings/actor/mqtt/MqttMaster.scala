package wings.actor.mqtt

import java.util.UUID

import akka.actor._
import akka.event.Logging
import org.eclipse.paho.client.mqttv3._
import play.api.libs.json._
import wings.enrichments.UUIDHelper
import wings.enrichments.UUIDHelper._
import wings.actor.adapter.mqtt.paho
import wings.actor.adapter.mqtt.paho.{ActorPahoMqttAdapter, MqttMessage}
import wings.actor.mqtt.router.{MqttMessages, MqttRouter}
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

  val ConfigOutTopic = Topics.generalConfigOutTopic

  /**
   * This actor id
   */
  val id = VOIdentityManager.next

  /**
   * Used virtual identities. For a key K in used identities, exists a Virtual Object V where V.id = K
   */
  var usedIdentities = new HashSet[UUID]

  /**
   * This actor childs. @see usedIdentities
   */
  var childs = new HashMap[UUID, ActorRef]

  val broker = "tcp://192.168.33.10:1883"

  val mqttConection = context.actorOf(MqttRouter.props(broker))

  override def preStart(): Unit = {
    mqttConection ! MqttRouter.Subscribe(Topics.generalConfigOutTopic, self)
    logger.debug("Sending subscription to topic: {}", Topics.generalConfigOutTopic)
  }

  def receive = {
    case msg @ MqttMessage(topic, payload, qos, retained, duplicate) =>
      topic match {
        case Topics.ConfigOutTopicPattern(id) => onConfigOutTopic(msg)
        case m => logger.debug("Received not matching topic message from topic: {}", m)
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
          UUIDHelper.tryFromString(v) match {
            case Success(identity) =>
              usedIdentities contains identity match {
                case true =>
                  logger.debug("MqttMaster has detected that identity already exists: {}", identity)
                  mqttConection ! MqttRouter.Publish(rejectMessage(v))
                case false =>
                  logger.debug("MqttMaster creates proxy actor")
                  val actor = context.actorOf(MqttActor.props(identity.copy, mqttConection), "MqttActor")
                  registerVirtualObject(identity, actor)
                  mqttConection ! MqttRouter.Publish(ackMessage(v))
              }
            case Failure(e) =>
              logger.debug(s"MqttMaster has failed to convert identity to an UUID, error message: {}", e)
              mqttConection ! MqttRouter.Publish(rejectMessage(v))
          }
      }
    }
  }

  def rejectMessage(v: String) = MqttMessage(Topics.provisionalConfigInTopic(v), Json.toJson[Config]
    (NameAcquisitionReject("")).toString().getBytes, 2, false, false)

  def ackMessage(v: String) = MqttMessage(Topics.provisionalConfigInTopic(v), Json.toJson[Config]
    (NameAcquisitionAck("")).toString().getBytes, 2, false, false)

  def registerVirtualObject(id: UUID, ref: ActorRef): Unit = {
    usedIdentities += id
    childs += (id -> ref)
  }

}