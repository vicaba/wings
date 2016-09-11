package wings.virtualobject.infrastructure.serialization.json

import play.api.libs.functional.syntax._
import play.api.libs.json.{OFormat, OWrites, Reads, _}
import wings.virtualobject.domain.ActuateState
import wings.virtualobject.infrastructure.keys.ActuateStateKeys


object ActuateStateJson {

  implicit val actuateStateReads: Reads[ActuateState] = (
    (__ \ ActuateStateKeys.StateIdKey).read[String] and
      (__ \ ActuateStateKeys.DescriptionKey).readNullable[String]
    )(ActuateState.apply _)

  implicit val actuateStateWrites: OWrites[ActuateState] = (
    (__ \ ActuateStateKeys.StateIdKey).write[String] and
      (__ \ ActuateStateKeys.DescriptionKey).writeNullable[String]
    )(unlift(ActuateState.unapply _))

  implicit val ActuateStateFormat = OFormat(actuateStateReads, actuateStateWrites)

}
