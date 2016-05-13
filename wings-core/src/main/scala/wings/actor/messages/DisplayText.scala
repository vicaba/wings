package wings.actor.messages

import akka.actor.ActorRef

trait DisplayText

case class BroadcastDisplayText(groupId: Int, text: String) extends DisplayText

case class MulticastDisplayText(actors: Set[ActorRef], text: String) extends DisplayText

case class UnicastDisplayText(text: String) extends DisplayText
