package wings.actor.mqtt.router

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, Props, Stash}
import wings.actor.adapter.mqtt.paho.MqttMessage
import wings.actor.mqtt.router.MqttMessages.{Publish, Subscribe, Unsubscribe}

import scala.collection.immutable.HashMap

object MqttRouter {

  type Dictionary = Map[String, List[ActorRef]]

  def props(broker: String) = Props(MqttRouter(broker))

  trait RoutingMessage

  case class Subscribe(topic: String, ref: ActorRef) extends RoutingMessage

  case class Unsubscribe(topic: String, ref: ActorRef) extends RoutingMessage

}

private[router] object MqttMessages {

  case class Subscribe(topic: String)

  case class Unsubscribe(topic: String)

  case class Publish(msg: MqttMessage)

}

case class MqttRouter(broker: String)
  extends Actor
    with Stash {

  import MqttRouter._
  import context._

  val conn: ActorRef = context.actorOf(MqttConnection.props(broker, self))
  val wildcardWkr = context.actorOf(WildcardWorker.props())

  def router(routeeMap: Dictionary): Receive = {
    case MqttRouter.Subscribe(topic, ref) =>
      conn ! MqttMessages.Subscribe(topic)
      val list = ref :: routeeMap.getOrElse(topic, Nil)
      val map = routeeMap + (topic -> list)
      become(router(map));
    case MqttRouter.Unsubscribe(topic, ref) =>
      conn ! MqttMessages.Unsubscribe(topic)
      val list = routeeMap.getOrElse(topic, Nil).filter(_ != ref)
      val map = if (list.isEmpty) routeeMap - topic else routeeMap + (topic -> list)
      become(router(map));
    case p: Publish => conn ! p
    case mqttMsg: MqttMessage =>
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