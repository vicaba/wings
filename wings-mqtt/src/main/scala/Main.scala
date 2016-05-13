import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import wings.actor.mqtt.MqttMaster

object Main {

  def main(args: Array[String]) {
    val mqttSystem = ActorSystem("MqttSystem", ConfigFactory.load("playsystem"))

    val mqttMaster = mqttSystem.actorOf(MqttMaster.props())

  }
}
