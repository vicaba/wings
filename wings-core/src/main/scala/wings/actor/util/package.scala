package wings.actor

import akka.actor.{ExtendedActorSystem, Actor}

package object util {

  trait ActorUtilities extends Actor {
    def remoteAddress =
      self.path.toStringWithAddress(context.system.asInstanceOf[ExtendedActorSystem].provider.getDefaultAddress)
  }
}
