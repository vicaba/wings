package wings.actor.util

import akka.actor.{Actor, ExtendedActorSystem}

trait ActorUtilities extends Actor {

  def remoteAddress: String =
    self.path.toStringWithAddress(context.system.asInstanceOf[ExtendedActorSystem].provider.getDefaultAddress)

}