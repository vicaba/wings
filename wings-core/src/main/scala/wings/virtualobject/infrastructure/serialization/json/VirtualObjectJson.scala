package wings.virtualobject.infrastructure.serialization.json

import java.time.ZonedDateTime
import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{OFormat, OWrites, Reads, _}
import wings.model.ActorReferenced
import wings.model.virtual.virtualobject.actuate.ActuateCapability
import wings.virtualobject.domain.{SenseCapability, VirtualObject}
import wings.virtualobject.infrastructure.keys.VirtualObjectKeys
import wings.virtualobject.infrastructure.repository.mongodb.VOIdentityManager
import wings.virtualobject.infrastructure.serialization.json.Implicits._


object VirtualObjectJson {

  val VirtualObjectReads: Reads[VirtualObject] = (
    (__ \ VOIdentityManager.name).readNullable[UUID] and
      (__ \ VirtualObjectKeys.VOIDKey).read[UUID] and
      (__ \ VirtualObjectKeys.PVOIDKey).readNullable[UUID] and
      (__ \ ActorReferenced.ReferenceKey).readNullable[String] and
      (__ \ VirtualObjectKeys.ChildrenKey).readNullable[Array[String]] and
      (__ \ VirtualObjectKeys.PathKey).read[String] and
      (__ \ VirtualObjectKeys.MetadataKey).read[JsObject] and
      (__ \ VirtualObjectKeys.CreationTimeKey).read[ZonedDateTime] and
      (__ \ VirtualObjectKeys.DeletionTimeKey).readNullable[ZonedDateTime] and
      (__ \ VirtualObjectKeys.SenseCapabilityKey).readNullable[SenseCapability] and
      (__ \ VirtualObjectKeys.ActuateCapabilityKey).readNullable[ActuateCapability]
    )(VirtualObject.apply _)

  val VirtualObjectWrites: OWrites[VirtualObject] = (
    (__ \ VOIdentityManager.name).writeNullable[UUID] and
      (__ \ VirtualObjectKeys.VOIDKey).write[UUID] and
      (__ \ VirtualObjectKeys.PVOIDKey).writeNullable[UUID] and
      (__ \ ActorReferenced.ReferenceKey).writeNullable[String] and
      (__ \ VirtualObjectKeys.ChildrenKey).writeNullable[Array[String]] and
      (__ \ VirtualObjectKeys.PathKey).write[String] and
      (__ \ VirtualObjectKeys.MetadataKey).write[JsObject] and
      (__ \ VirtualObjectKeys.CreationTimeKey).write[ZonedDateTime] and
      (__ \ VirtualObjectKeys.DeletionTimeKey).writeNullable[ZonedDateTime] and
      (__ \ VirtualObjectKeys.SenseCapabilityKey).writeNullable[SenseCapability] and
      (__ \ VirtualObjectKeys.ActuateCapabilityKey).writeNullable[ActuateCapability]
    )(unlift(VirtualObject.unapply _))

   val VirtualObjectFormat = OFormat(VirtualObjectReads, VirtualObjectWrites)
}
