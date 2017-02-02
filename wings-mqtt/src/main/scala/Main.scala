import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import wings.actor.mqtt.MqttMaster
import wings.config.Config

object Main {

  def main(args: Array[String]) {

    val config = ConfigFactory.load(Config.Environment)

    val mqttSystem = ActorSystem("MqttSystem", config)

    val mqttMaster = mqttSystem.actorOf(MqttMaster.props(), "MqttMaster")

    println(config)

  }
}
