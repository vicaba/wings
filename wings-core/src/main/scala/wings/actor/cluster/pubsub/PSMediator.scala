package wings.actor.cluster.pubsub

import akka.actor._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator._
import akka.event.Logging

import wings.actor.cluster.pubsub.PSMediator._
import wings.actor.util.ActorUtilities

object PSMediator {
  def props(): Props = Props(PSMediator())
  case class PublishedMsg(topic: String, msg: Any)
  case class PublishMsg(topic: String, msg: Any)
  case class Referrer(referrer: ActorRef)
  case object Ping
}

case class PSMediator() extends Actor with ActorUtilities {

  import context._

  val name = "PSMediator"

  val logger = Logging(context.system, this)

  become(waitingForReferrer)
  val mediator: ActorRef = DistributedPubSub(context.system).mediator

  def waitingForReferrer: Receive = {
    case Referrer(r) => become(forwarderTo(r))
    case Ping        => sender() ! Ping
  }

  def forwarderTo(referrer: ActorRef): Receive = {
    case s: Subscribe if sender() == referrer =>
      logger.debug(s"From $name: Subscribed to: $s")
      mediator ! s.copy(ref = self)
    case p: PublishMsg =>
      logger.debug(s"From $name: Published message: $p")
      mediator ! Publish(p.topic, PublishedMsg(p.topic, p.msg))
    case pm: PublishedMsg =>
      logger.debug(s"From $name: Received published message: $pm")
      referrer ! pm
  }

  override def receive: Receive = {
    case s: Any => println("Hi")
  }
}
