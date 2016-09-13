package wings.actor.mqtt

import java.net.URI
import java.util.UUID

import akka.actor._
import akka.event.Logging
import org.eclipse.paho.client.mqttv3._
import play.api.libs.json._
import scaldi.Injectable._
import wings.actor.adapter.mqtt.paho
import wings.actor.adapter.mqtt.paho.{ActorPahoMqttAdapter, MqttMessage}
import wings.actor.mqtt.router.MqttRouter
import wings.actor.mqtt.{MqttTopics => Topics}
import wings.config.DependencyInjector._
import wings.virtualobject.agent.domain.messages.command.{NameAcquisitionAck, NameAcquisitionReject, NameAcquisitionRequest, RegisterVirtualObjectId}
import wings.virtualobject.agent.infrastructure.serialization.json.Implicits._
import wings.virtualobject.domain.VirtualObject

import scala.collection.immutable.{HashMap, HashSet}
import scala.util.Try

object MqttMaster {
  def props() = Props(MqttMaster())
}

case class MqttMaster() extends Actor with ActorPahoMqttAdapter {

  val logger = Logging(context.system, this)

  val ConfigOutTopic = Topics.generalConfigOutTopic

  /**
   * This actor id
   */
  val id = UUID.randomUUID()

  /**
   * Used virtual identities. For a key K in used identities, exists a Virtual Object V where V.id = K
   */
  var usedIdentities = new HashSet[VirtualObject.IdType]

  /**
   * This actor childs. @see usedIdentities
   */
  var childs = new HashMap[UUID, ActorRef]

  val broker = inject[URI](identified by 'MqttBroker).toString

  val mqttConection = context.actorOf(MqttRouter.props(broker))

  var count = 0

  override def preStart(): Unit = {
    mqttConection ! MqttRouter.Subscribe(Topics.generalConfigOutTopic, self)
    logger.debug("Sending subscription to topic: {}", Topics.generalConfigOutTopic)
  }

  def receive = {
    case msg @ MqttMessage(topic, payload, qos, retained, duplicate) =>
      topic match {
        case Topics.ConfigOutTopicPattern(id) =>
          onConfigOutTopic(msg)
          count = count + 1
          logger.info("Deployed Actors: {}", count)
        case m => logger.debug("Received non matching topic message from topic: {}", m)
      }
    case _ => logger.debug("Mqtt Master has received an unknown message")
  }

  override def connectionLost(throwable: Throwable): Unit = {}

  override def deliveryComplete(token: IMqttDeliveryToken): Unit = {}

  def onConfigOutTopic(mqttMsg: paho.MqttMessage) = {

    Try(Json.parse(mqttMsg.payloadAsString())).map { msg =>
      msg.validate[RegisterVirtualObjectId].map {
        case NameAcquisitionRequest(virtualObjectId) =>
          logger.info("MqttMaster has received a NameAcquisitionRequest")
              usedIdentities contains virtualObjectId match {
                case true =>
                  logger.debug("MqttMaster has detected that identity already exists: {}", virtualObjectId)
                  mqttConection ! MqttRouter.Publish(rejectMessage("", UUID.randomUUID()))
                case false =>
                  logger.debug("MqttMaster creates proxy actor with identity: {}", virtualObjectId)
                  val actor = context.actorOf(MqttActor.props(virtualObjectId, mqttConection), s"MqttActor-${virtualObjectId.toString}")
                  registerVirtualObject(virtualObjectId, actor)
                  mqttConection ! MqttRouter.Publish(ackMessage(virtualObjectId))
              }

      }
    }
  }

  def rejectMessage(topic: String, virtualObjectId: VirtualObject.IdType) =
    MqttMessage(Topics.provisionalConfigInTopic(topic), Json.toJson(NameAcquisitionReject(virtualObjectId)).toString().getBytes, 2, false, false)

  def ackMessage(virtualObjectId: VirtualObject.IdType) =
    MqttMessage(Topics.provisionalConfigInTopic(virtualObjectId), Json.toJson(NameAcquisitionAck(virtualObjectId)).toString().getBytes, 2, false, false)

  def registerVirtualObject(id: VirtualObject.IdType, ref: ActorRef): Unit = {
    usedIdentities += id
    childs += (id -> ref)
  }

}