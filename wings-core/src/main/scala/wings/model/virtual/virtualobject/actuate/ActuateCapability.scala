package wings.model.virtual.virtualobject.actuate

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class ActuateCapability(name: String, states: Array[ActuateState])

object ActuateCapability {

  val NameKey = "name"
  val StatesKey = "states"

  val actuateCapabilityReads: Reads[ActuateCapability] = (
      (__ \ NameKey).read[String] and
      (__ \ StatesKey).read[Array[ActuateState]]
    )(ActuateCapability.apply _)

  val actuateCapabilityWrites: OWrites[ActuateCapability]  = (
      (__ \ NameKey).write[String] and
      (__ \ StatesKey).write[Array[ActuateState]]
    )(unlift(ActuateCapability.unapply _))

  implicit val actuateCapabilityFormat = OFormat(actuateCapabilityReads, actuateCapabilityWrites)
}
