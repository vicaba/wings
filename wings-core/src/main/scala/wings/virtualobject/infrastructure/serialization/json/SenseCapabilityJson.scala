package wings.virtualobject.infrastructure.serialization.json

import play.api.libs.functional.syntax._
import play.api.libs.json.{OWrites, Reads, _}

import wings.virtualobject.domain.SenseCapability
import wings.virtualobject.infrastructure.keys.SenseCapabilityKeys

object SenseCapabilityJson {

  val SenseCapabilityReads: Reads[SenseCapability] = (
    (__ \ SenseCapabilityKeys.NameKey).read[String] and
      (__ \ SenseCapabilityKeys.UnitKey).read[String]
  )(SenseCapability.apply _)

  val SenseCapabilityWrites: OWrites[SenseCapability] = (
    (__ \ SenseCapabilityKeys.NameKey).write[String] and
      (__ \ SenseCapabilityKeys.UnitKey).write[String]
  )(unlift(SenseCapability.unapply _))

  val SenseCapabilityFormat = OFormat(SenseCapabilityReads, SenseCapabilityWrites)

}
