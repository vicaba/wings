package wings.virtualobject.agent.domain

import java.util.UUID

import akka.actor._
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, Unsubscribe}
import akka.event.Logging
import akka.remote.RemoteScope
import wings.actor.cluster.pubsub.PSMediator
import wings.actor.cluster.pubsub.PSMediator.{PublishMsg, PublishedMsg, Referrer}
import wings.virtualobject.agent.domain.messages.command._
import wings.virtualobject.agent.domain.messages.event.VirtualObjectSensed

object ArchitectureDriver {
  def props(virtualObjectId: UUID, continuation: ActorRef) = Props(ArchitectureDriver(virtualObjectId, continuation))
  val name = "ArchitectureDriver"
}

case class ArchitectureDriver(virtualObjectId: UUID, continuation: ActorRef) extends Actor {

  import context._

  val logger = Logging(context.system, this)

  override def preStart(): Unit = {
    val mediator = context.actorOf(PSMediator.props().withDeploy(Deploy(scope = RemoteScope(AddressFromURIString("akka.tcp://PubSubCluster@127.0.0.1:3000")))), "pubsub")
    mediator ! Referrer(self)
    logger.debug("Architecture Driver deployed")
    become(continuationState(mediator))
  }

  override def receive: Receive = {
    case a: Any => println(a)
  }

  def continuationState(mediator: ActorRef): Receive = {

    val toDeviceReceive: PartialFunction[Any, Unit] = {
      case sv: VirtualObjectSensed =>
        logger.debug("{}. Sensed Value received: {}", ArchitectureDriver.name, sv)
        continuation ! MsgEnv.ToDevice(sv)
      case voActuate: ActuateOnVirtualObject =>
        logger.debug("{}. ActuateOnVirtualObject received: {}", ArchitectureDriver.name, voActuate)
        continuation ! MsgEnv.ToDevice(voActuate)
      case a: Any => println(s"Message not known $a")
    }

    val toArchReceive: PartialFunction[Any, Unit] = {
      case command: ManageVirtualObject => onVoManagementCommand(command, mediator)
      case sv: VirtualObjectSensed =>
        mediator ! PublishMsg(sv.voId.toString, sv)
        logger.debug("{}. Sensed Value Published", ArchitectureDriver.name)
      case voWatch: WatchVirtualObject =>
        mediator ! Subscribe(voWatch.path, self)
        logger.debug("{}. Subscribed to path {}", ArchitectureDriver.name, voWatch.path)
      case voActuate: ActuateOnVirtualObject =>
        val actuatePath = voActuate.path + "/a"
        mediator ! PublishMsg(actuatePath, voActuate)
        logger.debug("{}. Published to path {}", ArchitectureDriver.name, actuatePath)
      case a: Any => println(s"Message not known $a")
    }

    val receive: PartialFunction[Any, Unit] = {
      case MsgEnv.ToArch(msg) => toArchReceive(msg)
      case PublishedMsg(_, msg) => toDeviceReceive(msg)
      case a: Any => println(s"Message not known $a")
    }

    receive

  }

  def onVoManagementCommand(manageVirtualObject: ManageVirtualObject, pubSubMediator: ActorRef) = {
    def actuatePathOf(virtualObjectId: String) = virtualObjectId + "/a"
    manageVirtualObject match {
      case CreateVirtualObject(virtualObjectId) => pubSubMediator ! Subscribe(actuatePathOf(virtualObjectId.toString), self)
      case RemoveVirtualObject(virtualObjectId) => pubSubMediator ! Unsubscribe(actuatePathOf(virtualObjectId.toString), self)
    }
  }

}