/* package wings.actor.mqtt

import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory


object MqttMain {

  def main(args: Array[String]) {
    val mqttSystem = ActorSystem("MqttSystem", ConfigFactory.load("remote"))
    mqttSystem.actorOf(Props(new SuperRandomActor()))


    //val mqttMaster = mqttSystem.actorOf(Props(new MqttMaster()))
    //mqttSystem.actorOf(PSMediator.props(), "pubsubmediator")

    //val mqttActor = mqttSystem.actorOf(Props(new MqttActor(VirtualIdentity("+"), "tcp://192.168.33.11:1883")))

    //mqttActor ! """{"msgType": "config", "action": "nameAcquisitionRequest", "value": "newmqttactor"}"""

  }

} */
