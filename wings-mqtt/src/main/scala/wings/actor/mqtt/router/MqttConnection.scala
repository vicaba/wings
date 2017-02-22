package wings.actor.mqtt.router

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props, Stash}
import akka.event.Logging

import wings.actor.adapter.mqtt.paho.{ActorPahoMqttAdapter, MqttMessage}
import wings.enrichments.UUIDHelper.UUIDEnrichment
import wings.util.actor.Stash

import org.eclipse.paho.client.mqttv3._
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

object MqttConnection {
  def props(broker: String, router: ActorRef): Props = Props(MqttConnection(broker, router))
}

case class MqttConnection(broker: String, router: ActorRef) extends Actor with ActorPahoMqttAdapter with Stash {

  import MqttMessages._
  import wings.actor.adapter.mqtt.paho.PahoMqttAdapter._
  import context._

  case class Connection(client: IMqttAsyncClient, connOpts: MqttConnectOptions)

  val logger = Logging(context.system, this)

  val id: UUID                       = UUID.randomUUID()
  val topicCounter: Map[String, Int] = Map[String, Int]()

  def connectToBroker(): IMqttToken = {
    val conn = Connection(
      new MqttAsyncClient(broker, id.toBase64, new MemoryPersistence()),
      new MqttConnectOptions()
    )
    conn.connOpts.setCleanSession(true)
    conn.client.setCallback(this)
    conn.client.connect(
      conn.connOpts,
      null,
      new IMqttActionListener {

        override def onFailure(iMqttToken: IMqttToken, throwable: Throwable): Unit = {
          logger.warning("Could not connect to broker with ip: {}\nStack trace:\n {}", broker, throwable.getMessage)
          throw new Exception()
        }

        override def onSuccess(iMqttToken: IMqttToken): Unit = {
          logger.debug("Connected to broker with ip: {}", broker)
          self ! Stash.UnStash
          become(connected(conn, topicCounter))
        }
      }
    )
  }

  override def preStart(): Unit = connectToBroker()

  def notConnected: Receive = {
    case Stash.UnStash => unstashAll()
    case m: Any =>
      logger.debug("Still not connected to broker, stashing message: {}", m.toString)
      stash()
  }

  def connected(conn: Connection, topicCounter: Map[String, Int]): Receive = {
    case Subscribe(topic) =>
      conn.client.subscribe(topic, 2)
      val count    = topicCounter.getOrElse(topic, 0) + 1
      val tCounter = topicCounter + (topic -> count)
      become(connected(conn, tCounter))
      logger.debug("Subscribed to topic {}", topic)
    case Unsubscribe(topic) =>
      conn.client.unsubscribe(topic)
      val count    = topicCounter.getOrElse(topic, 0) - 1
      val tCounter = if (count == -1) topicCounter - topic else topicCounter + (topic -> count)
      become(connected(conn, tCounter))
      logger.debug("UnSubscribed from topic {}", topic)
    case MqttRouter.Publish(msg) =>
      conn.client.publish(msg.topic, msg)
      logger.debug("Published at topic: {} message:\n{}", msg.topic, msg)
    case msg: MqttMessage => router ! msg
    case _                =>
  }

  override def receive: Receive = notConnected

  override def deliveryComplete(token: IMqttDeliveryToken): Unit = {}

  override def connectionLost(throwable: Throwable): Unit = {
    logger.warning("Connection lost due to \n{}", throwable.getMessage)
    throw new Exception()
  }
}
