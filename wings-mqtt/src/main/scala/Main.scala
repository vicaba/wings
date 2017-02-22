import akka.actor.ActorSystem

import wings.actor.mqtt.MqttMaster
import wings.config.Config

import com.typesafe.config.ConfigFactory

object Main {

  def main(args: Array[String]) {

    val config = ConfigFactory.load(Config.Environment)

    val mqttSystem = ActorSystem("MqttSystem", config)

    val mqttMaster = mqttSystem.actorOf(MqttMaster.props(), "MqttMaster")

    println(config)

  }
}
