package wings.actor.mqtt

import java.net.URI
import java.util.UUID

import scala.collection.immutable.{HashMap, HashSet}
import scala.util.Try

import akka.actor._
import akka.event.Logging

import play.api.libs.json._

import wings.actor.adapter.mqtt.paho
import wings.actor.adapter.mqtt.paho.{ActorPahoMqttAdapter, MqttMessage}
import wings.actor.mqtt.router.MqttRouter
import wings.config.DependencyInjector._
import wings.virtualobject.domain.VirtualObject
import wings.virtualobjectagent.domain.messages.command.{
  NameAcquisitionAck,
  NameAcquisitionReject,
  NameAcquisitionRequest,
  RegisterVirtualObjectId
}
import wings.virtualobjectagent.infrastructure.messages.serialization.json.Implicits._

import org.eclipse.paho.client.mqttv3._
import scaldi.Injectable._

object MqttMaster {
  def props(): Props = Props(MqttMaster())
}

case class MqttMaster() extends Actor with ActorPahoMqttAdapter {

  val logger = Logging(context.system, this)

  val ConfigOutTopic: String = MqttTopics.generalConfigOutTopic

  /**
    * This actor id
    */
  val id: UUID = UUID.randomUUID()

  /**
    * Used virtual identities. For a key K in used identities, exists a Virtual Object V where V.id = K
    */
  var usedIdentities = new HashSet[VirtualObject.IdType]

  /**
    * This actor childs. @see usedIdentities
    */
  var childs: Map[UUID, ActorRef] = new HashMap[UUID, ActorRef]

  val broker: String = inject[URI](identified by 'MqttBroker).toString

  val mqttConnection: ActorRef = context.actorOf(MqttRouter.props(broker))

  var count = 0

  override def preStart(): Unit = {
    mqttConnection ! MqttRouter.Subscribe(MqttTopics.generalConfigOutTopic, self)
    logger.debug("Sending subscription to topic: {}", MqttTopics.generalConfigOutTopic)
  }

  def receive: PartialFunction[Any, Unit] = {
    case msg @ MqttMessage(topic, payload, qos, retained, duplicate) =>
      topic match {
        case MqttTopics.ConfigOutTopicPattern(id) =>
          onConfigOutTopic(msg)
          count = count + 1
          logger.info("Deployed Actors: {}", count)
        case m => logger.debug("Received non matching topic message from topic: {}", m)
      }
    case _ => logger.debug("Mqtt Master has received an unknown message")
  }

  override def connectionLost(throwable: Throwable): Unit = {}

  override def deliveryComplete(token: IMqttDeliveryToken): Unit = {}

  def onConfigOutTopic(mqttMsg: paho.MqttMessage): Try[JsResult[Unit]] = {

    Try(Json.parse(mqttMsg.payloadAsString())).map { msg =>
      msg.validate[RegisterVirtualObjectId].map {
        case NameAcquisitionRequest(virtualObjectId) =>
          logger.info("MqttMaster has received a NameAcquisitionRequest")
          usedIdentities contains virtualObjectId match {
            case true =>
              logger.debug("MqttMaster has detected that identity already exists: {}", virtualObjectId)
              mqttConnection ! MqttRouter.Publish(rejectMessage("", UUID.randomUUID()))
            case false =>
              logger.debug("MqttMaster creates proxy actor with identity: {}", virtualObjectId)
              val actor = context.actorOf(MqttActor.props(virtualObjectId, mqttConnection),
                                          s"MqttActor-${virtualObjectId.toString}")
              registerVirtualObject(virtualObjectId, actor)
              mqttConnection ! MqttRouter.Publish(ackMessage(virtualObjectId))
          }

      }
    }
  }

  def rejectMessage(topic: String, virtualObjectId: VirtualObject.IdType): MqttMessage =
    MqttMessage(MqttTopics.provisionalConfigInTopic(topic),
                Json.toJson(NameAcquisitionReject(virtualObjectId)).toString().getBytes,
                2,
                false,
                false)

  def ackMessage(virtualObjectId: VirtualObject.IdType): MqttMessage =
    MqttMessage(MqttTopics.provisionalConfigInTopic(virtualObjectId),
                Json.toJson(NameAcquisitionAck(virtualObjectId)).toString().getBytes,
                2,
                false,
                false)

  def registerVirtualObject(id: VirtualObject.IdType, ref: ActorRef): Unit = {
    usedIdentities += id
    childs += (id -> ref)
  }

}
