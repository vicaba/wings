package wings.test.mqttrouter

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import org.scalatest.FreeSpec
import wings.actor.mqtt.router.MqttRouter

class MqttRouterTest extends FreeSpec {

  implicit val system = ActorSystem("test-system")

  val router = system.actorOf(MqttRouter.props("tcp://192.168.33.10:1883"))

  "router subscribes to topics" in {
    router ! MqttRouter.Subscribe("a", TestProbe().ref)

    router ! MqttRouter.Subscribe("b", TestProbe().ref)
  }

}
