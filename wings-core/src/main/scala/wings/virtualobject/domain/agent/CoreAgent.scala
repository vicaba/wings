package wings.virtualobject.domain.agent

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props, Stash}
import akka.event.Logging
import org.scalactic.{Bad, Good}
import scaldi.Injectable._
import wings.actor.util.ActorUtilities
import wings.collection.mutable.tree.Tree
import wings.config.DependencyInjector._
import wings.model.virtual.virtualobject.VOTree
import wings.virtualobject.domain.VirtualObject
import wings.virtualobject.domain.agent.CoreAgentMessages.{ToArchitectureActor, ToDeviceActor}
import wings.virtualobject.domain.messages.command.{ActuateOnVirtualObject, CreateVirtualObject, VirtualObjectBasicDefinition, WatchVirtualObject}
import wings.virtualobject.domain.messages.event.VirtualObjectSensed
import wings.virtualobject.domain.messages.event.repository.VirtualObjectSensedRepository
import wings.virtualobject.domain.repository.VirtualObjectRepository

import scala.concurrent.Future
import scala.util.{Failure, Success}

object CoreAgentMessages {

  case class ToDeviceActor()

  case class ToArchitectureActor()

}

object CoreAgent {
  val name = "CoreAgent"
}

trait CoreAgent extends Actor with Stash with ActorUtilities {

  import context._

  val name = CoreAgent.name

  val virtualObjectId: UUID

  val toDeviceProps: Props

  val toArchitectureProps: Props

  val virtualObjectRepository: VirtualObjectRepository = inject[VirtualObjectRepository](identified by 'VirtualObjectRepository)

  val virtualObjectSensedRepository: VirtualObjectSensedRepository = inject[VirtualObjectSensedRepository](identified by 'VirtualObjectSensedRepository)

  val logger = Logging(context.system, this)

  override def preStart(): Unit = {
    val toDevice = actorOf(toDeviceProps)
    logger.debug("Driver Actor deployed")
    become(state1(toDevice))
  }

  def saveOrUpdateVo(vobd: VirtualObjectBasicDefinition): Future[Option[VirtualObject]] = {
    virtualObjectRepository.findById(vobd.id).flatMap {
      case None =>
        logger.debug("Virtual Object with id {} not found", virtualObjectId)
        val newVirtualObject = vobd.toVirtualObject
        virtualObjectRepository.create(newVirtualObject).map {
          case Good(o) =>
            logger.debug("Inserted {}", newVirtualObject)
            Some(o)
          case Bad(wr) =>
            logger.debug("Saving or updating a VirtualObject failed, message: {}", wr.loneElement.message)
            None
        }

      case someVirtualObject: Some[VirtualObject] =>
        logger.debug("Virtual Object with id {} found", someVirtualObject.get)
        // TODO: Handle the case where a virtualObject is found!
        Future.successful(someVirtualObject)

    }
  }

  def state1(toDevice: ActorRef): Receive = {

    val toDeviceReceive: PartialFunction[Any, Unit] = {
      case a: Any => println("Device. Received Any")
    }

    val toArchReceive: PartialFunction[Any, Unit] = {
      case vobd: VirtualObjectBasicDefinition =>
        val voTemp = vobd.toVirtualObject
        logger.debug("I'm about to save VirtualObject data for the first time")
        //Future.successful[Option[VirtualObject]](Some(voTemp)).onComplete {
        saveOrUpdateVo(vobd).onComplete {
          case Failure(e) =>
            logger.debug("Failed to save VirtualObject data for the first time. Reason: {}", e.getStackTrace)
            throw e
          //TODO: handle Failure
          case Success(optVo) =>
            logger.debug("Success in saving VirtualObject data for the first time.")
            optVo match {
              case None => logger.debug("VirtualObject data for the first time was empty.")
              case Some(vo) =>
                val toArchitecture = actorOf(toArchitectureProps)
                val createVo = CreateVirtualObject(virtualObjectId)
                val endpoints = PipelineEndPoints(toDevice, toArchitecture)
                endpoints ! createVo
                val tree = VOTree(vo)
                logger.info("Setup completed, becoming state2. I can handle messages now")
                unstashAll()
                become(state2(tree, endpoints))
            }
        }

      case a: Any =>
        stash()
        logger.debug("Arch. Received Any: {}", a)

    }

    val receive: PartialFunction[Any, Unit] = {
      case MsgEnv.ToDevice(msg) => toDeviceReceive(msg)
      case MsgEnv.ToArch(msg) => toArchReceive(msg)
      case ToDeviceActor => sender ! toDevice
      case a: Any => println("All. Received Any")
    }

    receive
  }

  def state2(voTree: Tree[VirtualObject], endpoints: PipelineEndPoints): Receive = {
    val toDevice = endpoints.toDevice
    val toArchitecture = endpoints.toArchitecture

    val toDeviceReceive: PartialFunction[Any, Unit] = {
      case voActuate: ActuateOnVirtualObject =>
        logger.info("Sending an {} from {} to Device", voActuate.getClass, name)
        toDevice ! MsgEnv.ToDevice(voActuate)
      case sensedValue: VirtualObjectSensed =>
        logger.info("Sending an {} from {} to Device", sensedValue.getClass, name)
        toDevice ! MsgEnv.ToDevice(sensedValue)
    }

    val toArchReceive: PartialFunction[Any, Unit] = {
      case vobd: VirtualObjectBasicDefinition =>
        val parentVoTree = vobd.parentId.flatMap(pVoId => voTree.getWhere(_.id == pVoId))
        if (parentVoTree.isDefined) {
          val voTemp = vobd.toVirtualObject
          Future.successful[Option[VirtualObject]](Some(voTemp)).onComplete {
            //val voTree = parentVoTree.get
            //saveOrUpdateVo(m).onComplete {
            case Failure(e) => logger.error("Error saving vo with id: {}", vobd.id.toString)
            case Success(optVo) =>
              optVo match {
                case None => //TODO: handle Failure
                case Some(vo) =>
                  val createVo = CreateVirtualObject(vo.id) // TODO: Calling get is unsafe
                  endpoints ! createVo
                  voTree.add(vo)
                  become(state2(voTree, endpoints))
              }
          }
        }
      case virtualObjectSensed: VirtualObjectSensed =>
        logger.info("Sending an {} from {} to Arch", virtualObjectSensed.getClass, name)
        virtualObjectSensedRepository.create(virtualObjectSensed)
        toArchitecture ! MsgEnv.ToArch(virtualObjectSensed)
      case voWatch: WatchVirtualObject =>
        logger.info("Sending an {} from {} to Arch", voWatch.getClass, name)
        toArchitecture ! MsgEnv.ToArch(voWatch)
      case voActuate: ActuateOnVirtualObject =>
        logger.info("Sending an {} from {} to Arch", voActuate.getClass, name)
        toArchitecture ! MsgEnv.ToArch(voActuate)

    }

    val receive: PartialFunction[Any, Unit] = {
      case MsgEnv.ToDevice(msg) => toDeviceReceive(msg)
      case MsgEnv.ToArch(msg) => toArchReceive(msg)
      case ToDeviceActor => sender ! toDevice
      case ToArchitectureActor => sender ! toArchitecture
    }

    receive
  }

  // TODO: define behaviour when device can't connect to any service
  def unconnected: Receive = {
    case _ =>
  }

  def receive = {
    unconnected
  }

}
