package wings.actor.mqtt.router

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, Props, Stash}
import wings.actor.adapter.mqtt.paho.MqttMessage
import wings.actor.mqtt.router.MqttMessages.{Publish, Subscribe, Unsubscribe}

import scala.collection.immutable.HashMap

object MqttRouter {

  type Dictionary = Map[String, ActorRef]

  def props(broker: String) = Props(MqttRouter(broker))

  trait RoutingMessage

  case class AddRoutee(ref: ActorRef, topic: String) extends RoutingMessage

  case class RemoveRoutee(topic: String) extends RoutingMessage

}

object MqttMessages {

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
    case AddRoutee(ref, topic) => become(router(routeeMap + (topic -> ref)))
    case RemoveRoutee(topic) => become(router(routeeMap - topic))
    case s: Subscribe => conn ! s
    case us: Unsubscribe => conn ! us
    case p: Publish => conn ! p
    case mqttMsg: MqttMessage => routeeMap.get(mqttMsg.topic).foreach(_ ! mqttMsg); wildcardWkr ! WildcardWorker.Work(routeeMap, mqttMsg)
  }

  override def receive = router(HashMap[String, ActorRef]())

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

    dic.foreach { case (t, ref) =>
      val regex = t.replace("+", "(.+)").r
      t match {
        case regex(all@_*) => all.foreach(part => if (!part.contains("/")) ref ! msg)
        case _ =>
      }
    }

  }
}