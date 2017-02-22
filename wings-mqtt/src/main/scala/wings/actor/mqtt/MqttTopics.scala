package wings.actor.mqtt

import java.util.UUID

import scala.util.matching.Regex

object MqttTopics {

  def dataOutTopic(voId: String): String = voId + "/data/out"

  def dataInTopic(voId: String): String = voId + "/data/in"

  def configInTopic(voId: String): String = voId + "/config/in"

  def configOutTopic(voId: String): String = voId + "/config/out"

  def dataOutTopic(voId: UUID): String = dataOutTopic(voId.toString)

  def dataInTopic(voId: UUID): String = dataInTopic(voId.toString)

  def configInTopic(voId: UUID): String = configInTopic(voId.toString)

  def configOutTopic(voId: UUID): String = configOutTopic(voId.toString)

  val generalConfigOutTopic: String = "+/i/config/out"

  val ConfigOutTopicPattern: Regex = "(.+)/i/config/out".r

  def provisionalConfigOutTopic(voId: String): String = s"$voId/i/config/out"

  val generalConfigInTopic: String = "+/i/config/in"

  def provisionalConfigInTopic(voId: String): String = s"$voId/i/config/in"

  def provisionalConfigOutTopic(voId: UUID): String = provisionalConfigOutTopic(voId.toString)

  def provisionalConfigInTopic(voId: UUID): String = provisionalConfigInTopic(voId.toString)

}
