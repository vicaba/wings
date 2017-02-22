package wings.virtualobject.infrastructure.serialization.json

import play.api.libs.functional.syntax._
import play.api.libs.json.{OFormat, OWrites, Reads, _}
import play.api.libs.json.Reads._

import wings.virtualobject.domain.{ActuateCapability, ActuateState}
import wings.virtualobject.infrastructure.keys.ActuateCapabilityKeys
import wings.virtualobject.infrastructure.serialization.json.Implicits._

object ActuateCapabilityJson {

  val actuateCapabilityReads: Reads[ActuateCapability] = (
    (__ \ ActuateCapabilityKeys.NameKey).read[String] and
      (__ \ ActuateCapabilityKeys.StatesKey).read[Array[ActuateState]]
  )(ActuateCapability.apply _)

  val actuateCapabilityWrites: OWrites[ActuateCapability] = (
    (__ \ ActuateCapabilityKeys.NameKey).write[String] and
      (__ \ ActuateCapabilityKeys.StatesKey).write[Array[ActuateState]]
  )(unlift(ActuateCapability.unapply _))

  val ActuateCapabilityFormat = OFormat(actuateCapabilityReads, actuateCapabilityWrites)
}
