
import akka.actor.ActorSystem

import wings.config.Config

import com.typesafe.config.ConfigFactory



object Main {

  def main(args: Array[String]) {

    val config = ConfigFactory.load(Config.Environment)
    val seed   = ActorSystem("PubSubCluster", config)

  }

}
