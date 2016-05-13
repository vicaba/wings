package wings.actor.mqtt

import java.util.UUID

import akka.actor.{ActorRef, Actor}
import akka.actor.Actor.Receive

/**
  * Created by vicaba on 04/12/15.
  */
class EmptyDT(virtualObjectId: UUID, continuation: ActorRef) extends Actor {
  override def receive: Receive = {
    case msg: Any => continuation ! msg
  }
}
