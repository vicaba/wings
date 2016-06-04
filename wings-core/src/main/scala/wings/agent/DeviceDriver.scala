package wings.agent

import akka.actor.{ActorRef, Actor}
import akka.dispatch.sysmsg.Create
import akka.event.Logging
import wings.actor.pipeline.MsgEnv
import wings.agent.commands.{RemoveVo, CreateVo, VoManagementCommand}
import scala.reflect.ClassTag

object DeviceDriver {
  val name = "DeviceDriver"
}

/**
  * Abstraction over Actors that directly communicate with device protocols
  */
trait DeviceDriver extends Actor {

  val logger = Logging(context.system, this)

  /**
    * Information about the specific protocol. It exposes how to communicate with the device.
    */
  type DeviceConnectionContext

  /**
    * This type abstracts over the type used by the third party implementation to communicate that a message has been received.
    */
  type DeviceMessageType

  /**
    * Provides basic implementation of how messages should be handled in a bidirectional manner.
    *
    * @param dc           The device connection context.
    * @param continuation The continuation Actor to forward a message received from the device to.
    * @return A PartialFunction (Receive) that handles messages going on both directions.
    */
  def driverState(dc: DeviceConnectionContext, continuation: ActorRef)(implicit t: ClassTag[DeviceMessageType]): Receive = {
    case deviceMsg: DeviceMessageType =>
      logger.debug("{}. Message: {} received from {}, forwarding to architecture with path: {}", DeviceDriver
        .name, deviceMsg, sender.path, continuation.path)
      logger.debug("{}. Class of Message is {}", DeviceDriver.name, deviceMsg.getClass.getName)
      toArchitectureReceive(dc, continuation)(deviceMsg)
    case MsgEnv.ToDevice(msg) =>
      logger.debug("{}. Message: {} received from {}, forwarding to device", DeviceDriver.name, msg, sender.path)
      msg match {
        case command: VoManagementCommand => onVoManageCommand(command)
        case _ => toDeviceReceive(dc)(msg)
      }
  }

  def onVoManageCommand(command: VoManagementCommand) = {
    command match {
      case CreateVo(voId) =>
      case RemoveVo(voId) =>
    }
  }

  /**
    * All messages directed to the device are handled by this method.
    *
    * @param dc The device connection context needed in order to communicate to the device.
    * @return A PartialFunction that knows how to handle the messages received.
    */
  def toDeviceReceive(dc: DeviceConnectionContext): PartialFunction[Any, Unit]

  /**
    * All messages that come from the device are handled by this method.
    *
    * @param dc           The device connection context if needed.
    * @param continuation The continuation Actor towards the inner layers of the Architecture.
    * @return A PartialFunction that knows how to handle this Kind of messages.
    */
  def toArchitectureReceive(dc: DeviceConnectionContext, continuation: ActorRef): PartialFunction[DeviceMessageType, Unit]

}
