package wings.utils.test


import akka.actor._
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import akka.remote.RemoteScope
import wings.actor.cluster.pubsub.PSMediator
import wings.actor.cluster.pubsub.PSMediator._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case object Tick
trait Status
case object Active extends Status
case object UnActive extends Status

object SuperRandomActor {
  def props() = Props(SuperRandomActor())
}

case class SuperRandomActor() extends Actor {

  // Deploy a pubsubmediator in another system
  context.actorOf(RandomActor.props(), "RandomActor")

  override def receive: Actor.Receive = {
    case a: Any =>
  }
}

object RandomActor {
  def props() = Props(new RandomActor())
}

case class RandomActor() extends Actor {

  val pubSubMediator = context.actorOf(Props(PSMediator()).withDeploy(Deploy(scope = RemoteScope(AddressFromURIString("akka.tcp://PubSubCluster@127.0.0.1:3000")))), "pubsub")
  var pubSubMediatorStatus: Status = Active
  context.system.eventStream.subscribe(self, classOf[DeadLetter])

  context.watch(pubSubMediator)
  pubSubMediator ! Referrer(self)

  var counter = 0

  context.system.scheduler.schedule(1 second, 3 seconds) {
    self ! Tick
  }

  override def receive: Receive = {
    case Tick =>
      println(s"pubSubMediator path: $pubSubMediator")
      pubSubMediator ! Subscribe("0958232f-93c0-4559-9752-a362da8e07d3", self)
      counter = counter + 1
      if (counter == 2) pubSubMediator ! PublishMsg("0958232f-93c0-4559-9752-a362da8e07d3", s"HEY FROM ${self.path}")
    case pm: PublishedMsg =>
      println(s"${self.path} HAVE RECEIVED $pm")
      pubSubMediator ! PublishMsg(pm.topic, s"HEY FROM ${self.path}")
    //System.exit(-1)
    case Terminated(child) => println(s"$child terminated")
  }
}

