package wings.model.virtual.virtualobject.actuate

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class ActuateState(id: String, description: Option[String] = None)

object ActuateState {

  val StateIdKey = "stateId"
  val DescriptionKey = "desc"

  implicit val actuateStateReads: Reads[ActuateState] = (
    (__ \ StateIdKey).read[String] and
      (__ \ DescriptionKey).readNullable[String]
    )(ActuateState.apply _)

  implicit val actuateStateWrites: OWrites[ActuateState] = (
    (__ \ StateIdKey).write[String] and
      (__ \ DescriptionKey).writeNullable[String]
    )(unlift(ActuateState.unapply _))

  implicit val actuateStateFormat = OFormat(actuateStateReads, actuateStateWrites)
}
