package wings.agent

import java.net.URI
import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.{Actor, ActorRef, Props, Stash}
import akka.event.Logging
import play.api.libs.json.Json
import wings.actor.pipeline.MsgEnv
import wings.actor.util.ActorUtilities
import wings.agent.CoreAgentMessages.{ToArchitectureActor, ToDeviceActor}
import wings.agent.commands.CreateVo
import wings.collection.mutable.tree.Tree
import wings.m2m.VOMessage
import wings.model.virtual.operations.{VoActuate, VoWatch}
import wings.model.virtual.virtualobject.metadata.VOMetadata
import wings.model.virtual.virtualobject.sensed.SensedValue
import wings.model.virtual.virtualobject.VOTree
import wings.virtualobject.infrastructure.repository.mongodb.{VOIdentityManager, VirtualObjectMongoRepository}
import scaldi.Injectable._
import wings.config.DependencyInjector._
import wings.services.db.MongoEnvironment
import wings.virtualobject.domain.VirtualObject
import wings.virtualobject.infrastructure.keys.VirtualObjectKeys

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success}

case class PipelineEndpoints(toDevice: ActorRef, toArchitecture: ActorRef) {
  def !(message: Any)(implicit sender: ActorRef = Actor.noSender): Unit = {
    toDevice ! MsgEnv.ToDevice(message)
    toArchitecture ! MsgEnv.ToArch(message)
  }
}

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

  val mongoEnvironment: MongoEnvironment = inject[MongoEnvironment](identified by 'MongoEnvironment)

  val logger = Logging(context.system, this)

  override def preStart(): Unit = {
    val toDevice = actorOf(toDeviceProps)
    logger.debug("Driver Actor deployed")
    become(state1(toDevice))
  }

  def saveOrUpdateVo(vo: VOMessage): Future[Option[VirtualObject]] = {
    val voService = new VirtualObjectMongoRepository(mongoEnvironment.mainDb)(VOIdentityManager)
    voService.findOneByCriteria(Json.obj(VirtualObjectKeys.VOIDKey -> vo.voId)).flatMap {
      // TODO: Handle the case where a virtualObject is found!
      case None =>
        logger.debug("Virtual Object with id {} not found", virtualObjectId)
        val newVirtualObject = VirtualObject(
          Some(UUID.randomUUID()), vo.voId, vo.pVoId, Some(remoteAddress), vo.children,
          vo.path, None, ZonedDateTime.now(), None, vo.senseCapability, vo.actuateCapability
        )
        voService.create(newVirtualObject).map {

          case Right(o) =>
            logger.debug("Inserted {}", newVirtualObject)
            Some(o)
          case _ => None
        }

      case _ => Future(None)

    }
  }

  def state1(toDevice: ActorRef): Receive = {

    val toDeviceReceive: PartialFunction[Any, Unit] = {
      case a: Any => println("Device. Received Any")
    }

    val toArchReceive: PartialFunction[Any, Unit] = {
      case m: VOMessage =>
        val voTemp = VirtualObject(
          Some(UUID.randomUUID()), m.voId, m.pVoId, Some(remoteAddress), m.children,
          m.path, None, ZonedDateTime.now(), None, m.senseCapability, m.actuateCapability
        )
        logger.debug("I'm about to save VirtualObject data for the first time")
        //Future.successful[Option[VirtualObject]](Some(voTemp)).onComplete {
        saveOrUpdateVo(m).onComplete {
          case Failure(e) =>
            logger.debug("Failed to save VirtualObject data for the first time. Reason: {}", e.getStackTrace)
            throw e
          //TODO: handle Failure
          case Success(optVo) =>
            optVo match {
              case None => //TODO: handle Failure
              case Some(vo) =>
                val toArchitecture = actorOf(toArchitectureProps)
                val createVo = CreateVo(virtualObjectId.toString)
                val endpoints = PipelineEndpoints(toDevice, toArchitecture)
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

  def state2(voTree: Tree[VirtualObject], endpoints: PipelineEndpoints): Receive = {
    val toDevice = endpoints.toDevice
    val toArchitecture = endpoints.toArchitecture

    val toDeviceReceive: PartialFunction[Any, Unit] = {
      case m: VOMessage =>
      case voActuate: VoActuate =>
        logger.info("Sending an {} from {} to Device", voActuate.getClass, name)
        toDevice ! MsgEnv.ToDevice(voActuate)
      case sensedValue: SensedValue =>
        logger.info("Sending an {} from {} to Device", sensedValue.getClass, name)
        toDevice ! MsgEnv.ToDevice(sensedValue)
    }

    val toArchReceive: PartialFunction[Any, Unit] = {
      case m: VOMessage =>
        val parentVoTree = m.pVoId.flatMap(pVoId => voTree.getWhere(_.voId == pVoId))
        if (parentVoTree.isDefined) {
          val voTemp = VirtualObject(
            Some(UUID.randomUUID()), m.voId, m.pVoId, Some(remoteAddress), m.children,
            m.path, None, ZonedDateTime.now(), None, m.senseCapability, m.actuateCapability
          )
          Future.successful[Option[VirtualObject]](Some(voTemp)).onComplete {
            //val voTree = parentVoTree.get
          //saveOrUpdateVo(m).onComplete {
            case Failure(e) => logger.error("Error saving vo with id: {}", m.voId.toString)
            case Success(optVo) =>
              optVo match {
                case None => //TODO: handle Failure
                case Some(vo) =>
                  val createVo = CreateVo(vo.id.get.toString) // TODO: Calling get is unsafe
                  endpoints ! createVo
                  voTree.add(vo)
                  become(state2(voTree, endpoints))
              }
          }
        }
      case vom: VOMetadata =>
      case sensedValue: SensedValue =>
        logger.info("Sending an {} from {} to Arch", sensedValue.getClass, name)
        //val sensedValueService = SensedValueMongoService(mongoEnvironment.db1)(SensedValueIdentityManager)
        //sensedValueService.create(sensedValue)
        toArchitecture ! MsgEnv.ToArch(sensedValue)
      case voWatch: VoWatch =>
        logger.info("Sending an {} from {} to Arch", voWatch.getClass, name)
        toArchitecture ! MsgEnv.ToArch(voWatch)
      case voActuate: VoActuate =>
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
