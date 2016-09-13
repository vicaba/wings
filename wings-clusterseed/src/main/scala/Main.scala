import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory


/**
  * Created by vicaba on 16/10/15.
  */
object  Main {

  def main(args: Array[String]) {

    if (args.length == 1) {
      val node = args(0)
      node match {
        case "seed1" =>
          val seed1 = ActorSystem("PubSubCluster", ConfigFactory.parseString(s"akka.remote.netty.tcp.port=3000").
            withFallback(ConfigFactory.load("app")))
        case "seed2" =>
          val seed2 = ActorSystem("PubSubCluster", ConfigFactory.parseString(s"akka.remote.netty.tcp.port=3001").
            withFallback(ConfigFactory.load("app")))
      }
    }
  }

}
