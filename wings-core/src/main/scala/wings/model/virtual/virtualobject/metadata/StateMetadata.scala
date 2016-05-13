package wings.model.virtual.virtualobject.metadata

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import wings.model.virtual.virtualobject.actuate.{ActuateState, ActuateCapability}

case class StateMetadata(stateId: String, hname: String, hdesc: String, logic: Option[StateLogic])

object StateMetadata {

  val StateLogicKey = "logic"

  val stateMetadataReads: Reads[StateMetadata] = (
    (__ \ ActuateState.StateIdKey).read[String] and
      (__ \ VOMetadata.HNameKey).read[String] and
      (__ \ VOMetadata.HDescKey).read[String] and
      (__ \ StateLogicKey).readNullable[StateLogic]
    ) (StateMetadata.apply _)

  val stateMetadataWrites: OWrites[StateMetadata] = (
    (__ \ ActuateState.StateIdKey).write[String] and
      (__ \ VOMetadata.HNameKey).write[String] and
      (__ \ VOMetadata.HDescKey).write[String] and
      (__ \ StateLogicKey).writeNullable[StateLogic]
    ) (unlift(StateMetadata.unapply _))

  implicit val stateMetadataFormat = OFormat(stateMetadataReads, stateMetadataWrites)
}