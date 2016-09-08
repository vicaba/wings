package wings.test

import play.api.libs.json.Json
import wings.enrichments.UUIDHelper
import wings.m2m.VOMessage
import wings.model.virtual.virtualobject.actuate.{ActuateCapability, ActuateState}
import wings.virtualobject.domain.SenseCapability

/**
  * Created by vicaba on 18/02/16.
  */
object Main {

  def main(args: Array[String]) {
    val mqttVoId = "0958232f-93c0-4559-9752-a362da8e07d3"

    val uuid = UUIDHelper.tryFromString(mqttVoId).get

    val topic = s"$mqttVoId/config/out"

    println(topic)

    println(      Json.toJson(VOMessage(
      uuid,
      None,
      None,
      mqttVoId,
      Some(SenseCapability("power", "W")),
      Some(ActuateCapability("light", Array(ActuateState("on"), ActuateState("off"))))
    )).toString)
  }

}
