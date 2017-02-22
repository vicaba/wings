import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import wings.config.Config

object Main {

  def main(args: Array[String]) {

    val config = ConfigFactory.load(Config.Environment)
    val seed   = ActorSystem("PubSubCluster", config)

  }

}
