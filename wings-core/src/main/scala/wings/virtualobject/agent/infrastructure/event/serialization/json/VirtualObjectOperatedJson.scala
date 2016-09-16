package wings.virtualobject.agent.infrastructure.event.serialization.json

import java.time.ZonedDateTime
import java.util.UUID

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{OFormat, OWrites, Reads, _}
import wings.virtualobject.agent.domain.messages.event.{VirtualObjectActuated, VirtualObjectSensed}
import wings.virtualobject.agent.infrastructure.event.keys.VirtualObjectOperatedKeys
import wings.virtualobject.infrastructure.keys.{ActuateStateKeys, VirtualObjectKeys}

object VirtualObjectOperatedJson {


  val VirtualObjectActuatedReads: Reads[VirtualObjectActuated] = (
    (__ \ VirtualObjectOperatedKeys.IdKey).read[UUID] and
      (__ \ VirtualObjectKeys.IdKey).read[UUID] and
      (__ \ ActuateStateKeys.StateIdKey).read[String]
    ) (VirtualObjectActuated.apply _)

  val VirtualObjectActuatedWrites: OWrites[VirtualObjectActuated] = (
    (__ \ VirtualObjectOperatedKeys.IdKey).write[UUID] and
      (__ \ VirtualObjectKeys.IdKey).write[UUID] and
      (__ \ ActuateStateKeys.StateIdKey).write[String]
    ) (unlift(VirtualObjectActuated.unapply _))

  val VirtualObjectActuatedFormat = OFormat(VirtualObjectActuatedReads, VirtualObjectActuatedWrites)

  val VirtualObjectSensedReads: Reads[VirtualObjectSensed] = (
    (__ \ VirtualObjectOperatedKeys.IdKey).read[UUID] and
      (__ \ VirtualObjectKeys.IdKey).read[UUID] and
      (__ \ VirtualObjectOperatedKeys.ValueKey).read[String] and
      (__ \ VirtualObjectOperatedKeys.UnitKey).readNullable[String] and
      (__ \ VirtualObjectOperatedKeys.CreationTimeKey).readNullable[DateTime].map(_.getOrElse(DateTime.now()))
    ) (VirtualObjectSensed.apply _)

  val VirtualObjectSensedWrites: OWrites[VirtualObjectSensed] = (
    (__ \ VirtualObjectOperatedKeys.IdKey).write[UUID] and
      (__ \ VirtualObjectKeys.IdKey).write[UUID] and
      (__ \ VirtualObjectOperatedKeys.ValueKey).write[String] and
      (__ \ VirtualObjectOperatedKeys.UnitKey).writeNullable[String] and
      (__ \ VirtualObjectOperatedKeys.CreationTimeKey).write[DateTime]
    ) (unlift(VirtualObjectSensed.unapply _))

  val VirtualObjectSensedFormat = OFormat(VirtualObjectSensedReads, VirtualObjectSensedWrites)

}
