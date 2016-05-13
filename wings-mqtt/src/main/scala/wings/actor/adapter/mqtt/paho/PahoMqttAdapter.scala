package wings.actor.adapter.mqtt.paho

import akka.actor.Actor
import org.eclipse.paho.client._
import org.eclipse.paho.client.mqttv3.{IMqttDeliveryToken, MqttCallback}
import play.api.libs.json.{Json, JsValue}
import wings.actor.adapter.mqtt.paho.PahoMqttAdapter._

import scala.util.Try


/**
 * Wrapper for the Java MQTT message
 * @param topic the topic
 * @param payload the payload
 * @param qos the QoS level
 * @param retained is retained?
 * @param duplicate is a duplicate?
 */
case class MqttMessage(topic: String = "", payload: Array[Byte], qos: Int, retained: Boolean, duplicate: Boolean) {

  /**
   * Converts a Scala MQTT message to a Paho Java MQTT message
   * @return  a Paho MQTT message
   */
  def toJava: mqttv3.MqttMessage = {
    PahoMqttAdapter.scalaMqttMessage2JavaMqttMessage(this)
  }

  def payloadAsString(): String = {
    new String(payload)
  }

}

/**
 * Companion object for the Scala MQTT message
 */
object MqttMessage {

  def validateQos(qos: Int) = mqttv3.MqttMessage.validateQos(qos)

}

/**
 * MQTT adapter for actors
 */
trait ActorPahoMqttAdapter extends MqttCallback with Actor {

  def connectionLost(throwable: Throwable)

  def deliveryComplete(token: IMqttDeliveryToken)

  def messageArrived(topic: String, mqttMessage: mqttv3.MqttMessage): Unit = {
    this.self ! mqttMessage.toScala(topic)
  }

}

/**
 * MQTT adapter helper methods
 */
object PahoMqttAdapter {

  def msgToJson(msg: MqttMessage): Try[JsValue] = Try(Json.parse(msg.payloadAsString()))

  /**
   * Converts a Java Paho MQTT message to a Scala MQTT message
   * @param javaMqttMessage the Java MQTT message
   * @param topic the topic
   * @return a Scala MQTT message
   */
  implicit def javaMqttMessage2ScalaMqttMessage(javaMqttMessage: mqttv3.MqttMessage, topic: String = ""): MqttMessage =
    MqttMessage(
      topic,
      javaMqttMessage.getPayload,
      javaMqttMessage.getQos,
      javaMqttMessage.isRetained,
      javaMqttMessage.isDuplicate
    )

  /**
   * Converts a Scala MQTT message to a Java Paho MQTT message
   * @param scalaMqttMessage the scala MQTT message
   * @return a Java PAho MQTT message
   */
  implicit def scalaMqttMessage2JavaMqttMessage(scalaMqttMessage: MqttMessage): mqttv3.MqttMessage = {
    val message = new mqttv3.MqttMessage()
    message setPayload scalaMqttMessage.payload
    message setQos scalaMqttMessage.qos
    message setRetained scalaMqttMessage.retained
    message
  }

  /**
   * Java Paho MQTT message implicit improvements
   * @param msg the Java Paho MQTT message
   */
  implicit class PahoMqttMessageImprovements(msg: mqttv3.MqttMessage) {

    /**
     * Converts a Java Paho MQTT message to a Scala MQTT message
     * @return  a Scala MQTT message
     */
    def toScala: MqttMessage = {
      PahoMqttAdapter.javaMqttMessage2ScalaMqttMessage(msg)
    }

    /**
     * Converts a Java Paho MQTT message to a Scala MQTT message
     * @param topic the topic of the message
     * @return  a Scala MQTT message
     */
    def toScala(topic: String): MqttMessage = {
      PahoMqttAdapter.javaMqttMessage2ScalaMqttMessage(msg, topic)
    }

  }


}


