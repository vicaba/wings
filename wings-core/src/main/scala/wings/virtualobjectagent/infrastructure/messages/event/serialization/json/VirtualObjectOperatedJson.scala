package wings.virtualobjectagent.infrastructure.messages.event.serialization.json

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.{OFormat, OWrites, Reads, _}
import play.api.libs.json.Reads._

import wings.virtualobject.infrastructure.keys.{ActuateStateKeys, VirtualObjectKeys}
import wings.virtualobjectagent.domain.messages.event.{VirtualObjectActuated, VirtualObjectSensed}
import wings.virtualobjectagent.infrastructure.messages.event.keys.VirtualObjectOperatedKeys

import org.joda.time.{DateTime, Instant}

object VirtualObjectOperatedJson {

  val VirtualObjectActuatedReads: Reads[VirtualObjectActuated] = (
    (__ \ VirtualObjectOperatedKeys.IdKey).read[UUID] and
      (__ \ VirtualObjectOperatedKeys.VirtualObjectIdKey).read[UUID] and
      (__ \ ActuateStateKeys.StateIdKey).read[String]
  )(VirtualObjectActuated.apply _)

  val VirtualObjectActuatedWrites: OWrites[VirtualObjectActuated] = (
    (__ \ VirtualObjectOperatedKeys.IdKey).write[UUID] and
      (__ \ VirtualObjectKeys.IdKey).write[UUID] and
      (__ \ ActuateStateKeys.StateIdKey).write[String]
  )(unlift(VirtualObjectActuated.unapply _))

  val VirtualObjectActuatedFormat = OFormat(VirtualObjectActuatedReads, VirtualObjectActuatedWrites)

  val VirtualObjectSensedReads: Reads[VirtualObjectSensed] = (
    (__ \ VirtualObjectOperatedKeys.IdKey).read[UUID] and
      (__ \ VirtualObjectOperatedKeys.VirtualObjectIdKey).read[UUID] and
      (__ \ VirtualObjectOperatedKeys.ValueKey).read[String] and
      (__ \ VirtualObjectOperatedKeys.UnitKey).readNullable[String] and
      (__ \ VirtualObjectOperatedKeys.CreationTimeKey)
        .readNullable[Long]
        .map(_.map { date =>
          new DateTime(new Instant(date))
        } getOrElse DateTime.now())
  )(VirtualObjectSensed.apply _)

  val VirtualObjectSensedWrites: OWrites[VirtualObjectSensed] = (
    (__ \ VirtualObjectOperatedKeys.IdKey).write[UUID] and
      (__ \ VirtualObjectKeys.IdKey).write[UUID] and
      (__ \ VirtualObjectOperatedKeys.ValueKey).write[String] and
      (__ \ VirtualObjectOperatedKeys.UnitKey).writeNullable[String] and
      (__ \ VirtualObjectOperatedKeys.CreationTimeKey).write[DateTime]
  )(unlift(VirtualObjectSensed.unapply _))

  val VirtualObjectSensedFormat = OFormat(VirtualObjectSensedReads, VirtualObjectSensedWrites)

}
