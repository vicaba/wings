package wings.model.virtual.virtualobject.actuated

import java.util.UUID

import play.api.libs.json._
import play.api.libs.functional.syntax._

import wings.model.virtual.virtualobject.VO
import wings.model.virtual.virtualobject.actuate.ActuateState
import wings.model.{HasIdentity, IdentityManager}

object ActuatedValueIdentityManager extends IdentityManager[ActuatedValue, UUID] {
  override def name: String = "_id"

  override def next: UUID = UUID.randomUUID()

  override def of(entity: ActuatedValue): Option[UUID] = entity.id
}


case class ActuatedValue(
                          override val id: Option[UUID],
                          VOID: UUID,
                          stateId: String
                        ) extends HasIdentity[UUID]

object ActuatedValue {

  val actuatedValueReads: Reads[ActuatedValue] = (
    (__ \ ActuatedValueIdentityManager.name).readNullable[UUID] and
    (__ \ VO.VOIDKey).read[UUID] and
      (__ \ ActuateState.StateIdKey).read[String]
    )(ActuatedValue.apply _)

  val actuatedValueWrites: OWrites[ActuatedValue] = (
    (__ \ ActuatedValueIdentityManager.name).writeNullable[UUID] and
      (__ \ VO.VOIDKey).write[UUID] and
      (__ \ ActuateState.StateIdKey).write[String]
    )(unlift(ActuatedValue.unapply _))

  implicit val stateLogicFormat = OFormat(actuatedValueReads, actuatedValueWrites)

}