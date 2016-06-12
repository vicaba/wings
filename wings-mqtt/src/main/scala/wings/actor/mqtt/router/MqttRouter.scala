package wings.actor.mqtt.router

import akka.actor.{Actor, ActorRef, Props, Stash}
import akka.event.Logging
import wings.actor.adapter.mqtt.paho.MqttMessage

import scala.collection.immutable.HashMap

object MqttRouter {

  type Dictionary = Map[String, List[ActorRef]]

  def props(broker: String) = Props(MqttRouter(broker))

  trait RoutingMessage

  case class Subscribe(topic: String, ref: ActorRef) extends RoutingMessage

  case class Unsubscribe(topic: String, ref: ActorRef) extends RoutingMessage

  case class Publish(msg: MqttMessage)

}

private[router] object MqttMessages {

  case class Subscribe(topic: String)

  case class Unsubscribe(topic: String)

}

case class MqttRouter(broker: String)
  extends Actor
    with Stash {

  import MqttRouter._
  import context._

  val logger = Logging(context.system, this)

  val conn: ActorRef = context.actorOf(MqttConnection.props(broker, self))
  val wildcardWkr = context.actorOf(WildcardWorker.props())

  def router(routeeMap: Dictionary): Receive = {
    case MqttRouter.Subscribe(topic, ref) =>
      conn ! MqttMessages.Subscribe(topic)
      val list = ref :: routeeMap.getOrElse(topic, Nil)
      val map = routeeMap + (topic -> list)
      become(router(map));
      logger.debug("{} subscribed to topic {}", ref.path, topic)
    case MqttRouter.Unsubscribe(topic, ref) =>
      conn ! MqttMessages.Unsubscribe(topic)
      val list = routeeMap.getOrElse(topic, Nil).filter(_ != ref)
      val map = if (list.isEmpty) routeeMap - topic else routeeMap + (topic -> list)
      become(router(map));
    case p: Publish =>
      conn ! p
      logger.debug("Received publish message: {}", p)

    case mqttMsg: MqttMessage =>
      logger.debug("Message received at topic: {}.\nTopic exists in map: {}", mqttMsg.topic, routeeMap)
      routeeMap.get(mqttMsg.topic).foreach(_.foreach(_ ! mqttMsg))
      wildcardWkr ! WildcardWorker.Work(routeeMap, mqttMsg)
  }

  override def receive = router(HashMap[String, List[ActorRef]]())

}

private[router] object WildcardWorker {

  def props() = Props(WildcardWorker())

  case class Work(dic: MqttRouter.Dictionary, msg: MqttMessage)

}

private[router] case class WildcardWorker() extends Actor {

  override def receive: Receive = {
    case work: WildcardWorker.Work => onMqttMessage(work)
  }

  def onMqttMessage(work: WildcardWorker.Work) = {

    val msg = work.msg
    val dic = work.dic
    val topic = msg.topic

    dic.foreach { case (t, refList) =>
      val regex = t.replace("+", "(.+)").r
      t match {
        case regex(all@_*) => all.foreach(part => if (!part.contains("/")) refList.foreach(_ ! msg))
        case _ =>
      }
    }

  }
}