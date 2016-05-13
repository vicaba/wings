package wings.agent

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.{Props, ActorRef, Actor}
import akka.event.Logging
import play.api.libs.json.Json
import wings.actor.pipeline.MsgEnv
import wings.actor.util.ActorUtilities
import wings.agent.CoreAgentMessages.{ToArchitectureActor, ToDeviceActor}
import wings.agent.commands.CreateVo
import wings.collection.mutable.tree.Tree
import wings.m2m.VOMessage
import wings.model.lookup.database.mongodb.ActorSimpleLookupService
import wings.model.virtual.operations.{VoActuate, VoWatch}
import wings.model.virtual.virtualobject.metadata.{VOMetadataIdentityManager, VOMetadata}
import wings.model.virtual.virtualobject.sensed.SensedValue
import wings.model.virtual.virtualobject.{VOTree, VO, VOIdentityManager}
import wings.model.virtual.virtualobject.services.db.mongo.{VOMetadataMongoService, VirtualObjectMongoService}
import wings.services.db.MongoEnvironment

import scala.concurrent.duration._

import scala.concurrent.{Promise, Future}
import scala.util.{Success, Failure}

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

trait CoreAgent extends Actor with ActorUtilities {

  import context._

  val name = CoreAgent.name

  val virtualObjectId: UUID

  val toDeviceProps: Props

  val toArchitectureProps: Props

  val mongoEnvironment: MongoEnvironment

  val logger = Logging(context.system, this)

  override def preStart(): Unit = {
    val toDevice = actorOf(toDeviceProps)
    logger.debug("Driver Actor deployed")
    become(state1(toDevice))
  }

  def saveOrUpdateVo(vo: VOMessage): Future[Option[VO]] = {
    val voService = new VirtualObjectMongoService(mongoEnvironment.db1)(VOIdentityManager)
    voService.findOneByCriteria(Json.obj(VO.VOIDKey -> vo.voId)).flatMap {
      // TODO: Handle the case where a virtualObject is found!
      case None =>
        logger.debug(s"Virtual Object with id $virtualObjectId not found")
        val newVirtualObject = VO(
          Some(UUID.randomUUID()), vo.voId, vo.pVoId, Some(remoteAddress), vo.children,
          vo.path, None, ZonedDateTime.now(), None, vo.senseCapability, vo.actuateCapability
        )
        voService.create(newVirtualObject).map {

          case Right(o) =>
            logger.debug(s"inserted $newVirtualObject")
            Some(o)
          case _ => None
        }

      case _ => Future {None}

    }
  }

  def state1(toDevice: ActorRef): Receive = {

    val toDeviceReceive: PartialFunction[Any, Unit] = {
      case a: Any => // Do nothing
    }

    val toArchReceive: PartialFunction[Any, Unit] = {
      case m: VOMessage =>
        saveOrUpdateVo(m).onComplete {
          case Failure(e) => //TODO: handle Failure
          case Success(optVo) =>
            optVo match {
              case None => //TODO: handle Failure
              case Some(vo) =>
                val toArchitecture = actorOf(toArchitectureProps)
                val createVo = CreateVo(virtualObjectId.toString)
                val endpoints = PipelineEndpoints(toDevice, toArchitecture)
                endpoints ! createVo
                val tree = VOTree(vo)
                become(state2(tree, endpoints))
            }
        }
      case a: Any => // Do nothing
    }

    val receive: PartialFunction[Any, Unit] = {
      case MsgEnv.ToDevice(msg) => toDeviceReceive(msg)
      case MsgEnv.ToArch(msg) => toArchReceive(msg)
      case ToDeviceActor => sender ! toDevice
      case a: Any => // Do nothing
    }

    receive
  }

  def state2(voTree: Tree[VO], endpoints: PipelineEndpoints): Receive = {
    val toDevice = endpoints.toDevice
    val toArchitecture = endpoints.toArchitecture

    val toDeviceReceive: PartialFunction[Any, Unit] = {
      case m: VOMessage =>
      case voActuate: VoActuate =>
        logger.debug(s"From $name to device: Received: $voActuate")
        toDevice ! MsgEnv.ToDevice(voActuate)
      case sensedValue: SensedValue =>
        logger.debug(s"From $name to device: Received: $sensedValue")
        toDevice ! MsgEnv.ToDevice(sensedValue)
    }

    val toArchReceive: PartialFunction[Any, Unit] = {
      case m: VOMessage =>
        val parentVoTree = m.pVoId.flatMap(pVoId => voTree.getWhere(_.voId == pVoId))
        if (parentVoTree.isDefined) {
          val voTree = parentVoTree.get
          saveOrUpdateVo(m).onComplete {
            case Failure(e) => //TODO: handle Failure
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
        logger.debug(s"From $name to arch: Received: $sensedValue")
        toArchitecture ! MsgEnv.ToArch(sensedValue)
      case voWatch: VoWatch =>
        logger.debug(s"From $name to arch: Received: $voWatch")
        toArchitecture ! MsgEnv.ToArch(voWatch)
      case voActuate: VoActuate =>
        logger.debug(s"From $name to arch: Received: $voActuate")
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
