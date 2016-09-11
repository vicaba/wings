package wings.virtualobject.agent.domain

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.{Actor, ActorRef, Props, Stash}
import akka.event.Logging
import org.scalactic.{Bad, Good, One}
import play.api.libs.json.Json
import scaldi.Injectable._
import wings.actor.util.ActorUtilities
import wings.collection.mutable.tree.Tree
import wings.config.DependencyInjector._
import wings.m2m.VOMessage
import wings.model.virtual.virtualobject.VOTree
import wings.virtualobject.agent.domain.CoreAgentMessages.{ToArchitectureActor, ToDeviceActor}
import wings.virtualobject.agent.domain.messages.command.{ActuateOnVirtualObject, CreateVirtualObject, WatchVirtualObject}
import wings.virtualobject.agent.domain.messages.event.VirtualObjectSensed
import wings.virtualobject.domain.VirtualObject
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

  val logger = Logging(context.system, this)

  override def preStart(): Unit = {
    val toDevice = actorOf(toDeviceProps)
    logger.debug("Driver Actor deployed")
    become(state1(toDevice))
  }

  def saveOrUpdateVo(vo: VOMessage): Future[Option[VirtualObject]] = {
    virtualObjectRepository.findById(vo.voId).flatMap {
      case None =>
        logger.debug("Virtual Object with id {} not found", virtualObjectId)
        val newVirtualObject = VirtualObject(
          vo.voId, vo.pVoId, vo.children,
          vo.path, Json.obj(), ZonedDateTime.now(), None, vo.senseCapability, vo.actuateCapability
        )
        virtualObjectRepository.create(newVirtualObject).map {
          case Good(o) =>
            logger.debug("Inserted {}", newVirtualObject)
            Some(o)
          case Bad(wr) =>
            logger.debug("Saving or updating a VirtualObject failed, message: {}", wr.loneElement.message)
            None
        }

      case Some(o) =>
        logger.debug("Virtual Object with id {} found", o)
        // TODO: Handle the case where a virtualObject is found!
        Future(None)

    }
  }

  def state1(toDevice: ActorRef): Receive = {

    val toDeviceReceive: PartialFunction[Any, Unit] = {
      case a: Any => println("Device. Received Any")
    }

    val toArchReceive: PartialFunction[Any, Unit] = {
      case m: VOMessage =>
        val voTemp = VirtualObject(
          m.voId, m.pVoId, m.children,
          m.path, Json.obj(), ZonedDateTime.now(), None, m.senseCapability, m.actuateCapability
        )
        logger.debug("I'm about to save VirtualObject data for the first time")
        //Future.successful[Option[VirtualObject]](Some(voTemp)).onComplete {
        saveOrUpdateVo(m).onComplete {
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
      case m: VOMessage =>
      case voActuate: ActuateOnVirtualObject =>
        logger.info("Sending an {} from {} to Device", voActuate.getClass, name)
        toDevice ! MsgEnv.ToDevice(voActuate)
      case sensedValue: VirtualObjectSensed =>
        logger.info("Sending an {} from {} to Device", sensedValue.getClass, name)
        toDevice ! MsgEnv.ToDevice(sensedValue)
    }

    val toArchReceive: PartialFunction[Any, Unit] = {
      case m: VOMessage =>
        val parentVoTree = m.pVoId.flatMap(pVoId => voTree.getWhere(_.id == pVoId))
        if (parentVoTree.isDefined) {
          val voTemp = VirtualObject(
            m.voId, m.pVoId, m.children,
            m.path, Json.obj(), ZonedDateTime.now(), None, m.senseCapability, m.actuateCapability
          )
          Future.successful[Option[VirtualObject]](Some(voTemp)).onComplete {
            //val voTree = parentVoTree.get
            //saveOrUpdateVo(m).onComplete {
            case Failure(e) => logger.error("Error saving vo with id: {}", m.voId.toString)
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
      case sensedValue: VirtualObjectSensed =>
        logger.info("Sending an {} from {} to Arch", sensedValue.getClass, name)
        //val sensedValueService = SensedValueMongoService(mongoEnvironment.db1)(SensedValueIdentityManager)
        //sensedValueService.create(sensedValue)
        toArchitecture ! MsgEnv.ToArch(sensedValue)
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
