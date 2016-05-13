package wings.model.virtual.virtualobject.sense

import play.api.libs.functional.syntax._
import play.api.libs.json._
import wings.model.virtual.virtualobject.actuate.ActuateState

case class SenseCapability(name: String, unit: String)

object SenseCapability {

  val NameKey = "name"
  val UnitKey = "unit"

  val senseCapabilityReads: Reads[SenseCapability] = (
      (__ \ NameKey).read[String] and
      (__ \ UnitKey).read[String]
    )(SenseCapability.apply _)

  val senseCapabilityWrites: OWrites[SenseCapability] = (
      (__ \ NameKey).write[String] and
      (__ \ UnitKey).write[String]
    )(unlift(SenseCapability.unapply _))

  implicit val senseCapabilityFormat = OFormat(senseCapabilityReads, senseCapabilityWrites)
}
