package wings.actor

import akka.actor.{Actor, ExtendedActorSystem}

package object util {

  trait ActorUtilities extends Actor {
    def remoteAddress: String =
      self.path.toStringWithAddress(context.system.asInstanceOf[ExtendedActorSystem].provider.getDefaultAddress)
  }
}
