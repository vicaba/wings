package wings.virtualobject.domain.agent

import akka.actor.{Actor, ActorRef}

case class PipelineEndPoints(toDevice: ActorRef, toArchitecture: ActorRef) {
  def !(message: Any)(implicit sender: ActorRef = Actor.noSender): Unit = {
    toDevice ! MsgEnv.ToDevice(message)
    toArchitecture ! MsgEnv.ToArch(message)
  }
}

object MsgEnv {

  case class ToDevice[M](msg: M)

  case class ToArch[M](msg: M)

}