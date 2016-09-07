package wings.agent

import java.util.UUID

import akka.actor._
import akka.cluster.pubsub.DistributedPubSubMediator.{Unsubscribe, Subscribe}
import akka.event.Logging

import akka.remote.RemoteScope
import wings.actor.cluster.pubsub.PSMediator
import wings.actor.cluster.pubsub.PSMediator.{PublishedMsg, Referrer, PublishMsg}
import wings.actor.pipeline.MsgEnv
import wings.agent.commands.{RemoveVo, CreateVo, VoManagementCommand}
import wings.m2m.VOMessage
import wings.model.virtual.operations.{VoActuate, VoWatch}
import wings.model.virtual.virtualobject.actuated.ActuatedValue
import wings.model.virtual.virtualobject.metadata.VOMetadata
import wings.model.virtual.virtualobject.sensed.SensedValue

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
      case m: VOMessage =>
      case vom: VOMetadata =>
      case sv: SensedValue =>
        logger.debug("{}. Sensed Value received: {}", ArchitectureDriver.name, sv)
        continuation ! MsgEnv.ToDevice(sv)
      case voActuate: VoActuate =>
        logger.debug("{}. VoActuate received: {}", ArchitectureDriver.name, voActuate)
        continuation ! MsgEnv.ToDevice(voActuate)
      case a: Any => println(s"Message not known $a")
    }

    val toArchReceive: PartialFunction[Any, Unit] = {
      case command: VoManagementCommand => onVoManagementCommand(command, mediator)
      case m: VOMessage =>
      case vom: VOMetadata =>
      case sv: SensedValue =>
        mediator ! PublishMsg(sv.voId.toString, sv)
        logger.debug("{}. Sensed Value Published", ArchitectureDriver.name)
      case voWatch: VoWatch =>
        mediator ! Subscribe(voWatch.path, self)
        logger.debug("{}. Subscribed to path {}", ArchitectureDriver.name, voWatch.path)
      case voActuate: VoActuate =>
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

  def onVoManagementCommand(voManagementCommand: VoManagementCommand, pubSubMediator: ActorRef) = {
    def actuatePath(voId: String) = voId + "/a"
    voManagementCommand match {
      case CreateVo(voId) => pubSubMediator ! Subscribe(actuatePath(voId), self)
      case RemoveVo(voId) => pubSubMediator ! Unsubscribe(actuatePath(voId), self)
    }
  }

}