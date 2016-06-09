package wings.actor.mqtt.router

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props, Stash}
import akka.event.Logging
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.eclipse.paho.client.mqttv3._
import wings.actor.adapter.mqtt.paho.{ActorPahoMqttAdapter, MqttMessage}
import wings.enrichments.UUIDHelper.UUIDEnrichment

object MqttConnection {
  def props(broker: String, router: ActorRef) = Props(MqttConnection(broker, router))
}

case class MqttConnection(broker: String, router: ActorRef)
  extends Actor
    with ActorPahoMqttAdapter
    with Stash {

  import MqttMessages._
  import wings.actor.adapter.mqtt.paho.PahoMqttAdapter._
  import context._

  case class Connection(client: IMqttAsyncClient, connOpts: MqttConnectOptions)

  val id: UUID = UUID.randomUUID()
  val logger = Logging(context.system, this)

  def connectToBroker() = {
    val conn = Connection(
      new MqttAsyncClient(broker, id.toBase64, new MemoryPersistence()), new MqttConnectOptions()
    )
    conn.connOpts.setCleanSession(true)
    conn.client.setCallback(this)
    conn.client.connect(conn.connOpts, null, new IMqttActionListener {

      override def onFailure(iMqttToken: IMqttToken, throwable: Throwable): Unit = {
        logger.warning("Could not connect to broker with ip: {}\nStack trace:\n {}", broker, throwable.getMessage)
        throw new Exception()
      }

      override def onSuccess(iMqttToken: IMqttToken): Unit = {
        logger.debug("Connected to broker with ip: {}", broker)
        unstashAll()
        become(connected(conn))
      }
    })
  }

  override def preStart(): Unit = connectToBroker()

  def notConnected: Receive = {
    case _ => stash()
  }

  def connected(conn: Connection): Receive = {
    case Subscribe(topic) => conn.client.subscribe(topic, 2)
    case Unsubscribe(topic) => conn.client.unsubscribe(topic)
    case Publish(msg) => conn.client.publish(msg.topic, msg)
    case msg: MqttMessage => router ! msg
    case _ =>
  }

  override def receive: Receive = notConnected

  override def deliveryComplete(token: IMqttDeliveryToken): Unit = {}

  override def connectionLost(throwable: Throwable): Unit = {
    logger.warning("Connection lost due to \n{}", throwable.getMessage)
    throw new Exception()
  }
}