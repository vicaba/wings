package wings.model.virtual.virtualobject.sensed

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import wings.model.{HasIdentity, IdentityManager}
import wings.virtualobject.domain.ActuateState
import wings.virtualobject.infrastructure.keys.{ActuateStateKeys, VirtualObjectKeys}


object SensedValueIdentityManager extends IdentityManager[SensedValue, UUID] {
  override def name: String = "_id"

  override def next: UUID = UUID.randomUUID()

  override def of(entity: SensedValue): Option[UUID] = entity.id
}

case class SensedValue(
                      id: Option[UUID] = None,
                      voId: UUID,
                      value: String,
                      unit: Option[String] = None,
                      stateId: Option[String] = None
                      ) extends HasIdentity[UUID]

object SensedValue {

  val ValueKey = "value"

  val sensedValueReads: Reads[SensedValue] = (
      (__ \ SensedValueIdentityManager.name).readNullable[UUID] and
      (__ \ VirtualObjectKeys.VOIDKey).read[UUID] and
      (__ \ ValueKey).read[String] and
      (__ \ "unit").readNullable[String] and
        (__ \ ActuateStateKeys.StateIdKey).readNullable[String]
    )(SensedValue.apply _)

  val sensedValueWrites: OWrites[SensedValue] = (
      (__ \ SensedValueIdentityManager.name).writeNullable[UUID] and
      (__ \ VirtualObjectKeys.VOIDKey).write[UUID] and
      (__ \ ValueKey).write[String] and
      (__ \ "unit").writeNullable[String] and
        (__ \ ActuateStateKeys.StateIdKey).writeNullable[String]
    )(unlift(SensedValue.unapply _))

  implicit val sensedvalueFormat = OFormat(sensedValueReads, sensedValueWrites)

}
