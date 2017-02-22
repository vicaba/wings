package wings.client.actor.mqtt

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import play.api.libs.json.JsValue
import wings.actor.adapter.mqtt.paho.MqttMessage
import wings.actor.mqtt.router.MqttRouter
import wings.client.actor.ActorTestMessages.MessageSent

object MqttTestActor2 {

  def props(conn: ActorRef, testSender: ActorRef) = Props(MqttTestActor2(conn, testSender))

  object Messages {
    case class Subscribe(topic: String)
    case class Publish(topic: String, message: String)
    object Publish {
      def apply(topic: String, message: JsValue): Publish = new Publish(topic, message.toString)
    }
  }
}

case class MqttTestActor2(conn: ActorRef, testSender: ActorRef) extends Actor {

  val logger = Logging(context.system, this)

  override def receive: Receive = {
    case MqttTestActor2.Messages.Subscribe(topic) =>
      conn ! MqttRouter.Subscribe(topic, self)
      logger.debug("Subscribing to topic: {}", topic)
      println(s"Subscribing to topic: $topic")
      sender ! MessageSent
    case MqttTestActor2.Messages.Publish(topic, message) =>
      val msg = MqttMessage(topic, message.getBytes, 0, false, false)
      conn ! MqttRouter.Publish(msg)
      sender ! MessageSent
    case m: MqttMessage => testSender ! m
    case a: Any =>
      logger.debug("Received an unknown message: {}", a)
      println(s"Received an unknown message: $a")
  }

}
