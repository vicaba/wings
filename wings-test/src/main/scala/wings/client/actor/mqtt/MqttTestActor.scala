package wings.client.actor.mqtt

import akka.actor.{Actor, ActorRef, Props, Stash}
import org.eclipse.paho.client.mqttv3.{IMqttActionListener, IMqttDeliveryToken, IMqttToken}
import play.api.libs.json.JsValue
import wings.actor.adapter.mqtt.paho.PahoMqttAdapter._
import wings.actor.adapter.mqtt.paho.{ActorPahoMqttAdapter, MqttMessage}
import wings.actor.mqtt.MqttConnection
import wings.client.actor.ActorTestMessages.{LastMessage, MessageSent}

object MqttTestActor {

  def props(broker: String, mqttConnection: MqttConnection, testSender: ActorRef) =
    Props(MqttTestActor(broker, mqttConnection, testSender))

  object Messages {
    case class Subscribe(topic: String)
    case class Publish(topic: String, message: String)
    object Publish {
      def apply(topic: String, message: JsValue): Publish = new Publish(topic, message.toString)
    }
  }
}

case class MqttTestActor(broker: String, mqttConnection: MqttConnection, testSender: ActorRef)
    extends ActorPahoMqttAdapter
    with Stash {

  import context._

  override def preStart(): Unit = {
    mqttConnection.connOpts.setCleanSession(false)
    mqttConnection.client.setCallback(this)
    mqttConnection.client.connect(
      mqttConnection.connOpts,
      null,
      new IMqttActionListener {

        override def onFailure(iMqttToken: IMqttToken, throwable: Throwable): Unit = {}

        override def onSuccess(iMqttToken: IMqttToken): Unit = {
          self ! "Connect"
        }
      }
    )
  }

  override def connectionLost(throwable: Throwable): Unit = {}

  override def deliveryComplete(token: IMqttDeliveryToken): Unit = {}

  override def receive: Actor.Receive = {
    case "Connect" =>
      unstashAll()
      become(connectedState(mqttConnection))
    case _ => stash()
  }

  def connectedState(mqttConnection: MqttConnection): Receive = {
    case MqttTestActor.Messages.Subscribe(topic) =>
      mqttConnection.client.subscribe(topic, 2)
      sender ! MessageSent
    case MqttTestActor.Messages.Publish(topic, message) =>
      val msg = MqttMessage(topic, message.getBytes, 2, false, false)
      mqttConnection.client.publish(topic, msg)
      sender ! MessageSent
    case m: MqttMessage => testSender ! m
    case _              => println("I have received a message that I don't understand")
  }

}
