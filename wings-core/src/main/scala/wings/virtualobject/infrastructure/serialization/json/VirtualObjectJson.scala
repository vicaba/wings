package wings.virtualobject.infrastructure.serialization.json

import java.time.ZonedDateTime
import java.util.UUID

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{OFormat, OWrites, Reads, _}
import play.api.libs.json.Writes._
import wings.virtualobject.domain.{ActuateCapability, SenseCapability, VirtualObject}
import wings.virtualobject.infrastructure.keys.VirtualObjectKeys
import wings.virtualobject.infrastructure.serialization.json.Implicits._
import wings.json.Additions._


object VirtualObjectJson {

  val VirtualObjectReads: Reads[VirtualObject] = (
      (__ \ VirtualObjectKeys.IdKey).read[UUID] and
      (__ \ VirtualObjectKeys.ParentIdKey).readNullable[UUID] and
      (__ \ VirtualObjectKeys.ChildrenKey).readNullable[Array[String]] and
      (__ \ VirtualObjectKeys.PathKey).read[String] and
      (__ \ VirtualObjectKeys.MetadataKey).readNullable[JsObject].map(_.getOrElse(Json.obj())) and
      (__ \ VirtualObjectKeys.CreationTimeKey).read[DateTime] and
      (__ \ VirtualObjectKeys.DeletionTimeKey).readNullable[DateTime] and
      (__ \ VirtualObjectKeys.SenseCapabilityKey).readNullable[SenseCapability] and
      (__ \ VirtualObjectKeys.ActuateCapabilityKey).readNullable[ActuateCapability]
    )(VirtualObject.apply _)

  val VirtualObjectWrites: OWrites[VirtualObject] = (
      (__ \ VirtualObjectKeys.IdKey).write[UUID] and
      (__ \ VirtualObjectKeys.ParentIdKey).writeNullable[UUID] and
      (__ \ VirtualObjectKeys.ChildrenKey).writeNullable[Array[String]] and
      (__ \ VirtualObjectKeys.PathKey).write[String] and
      (__ \ VirtualObjectKeys.MetadataKey).writeEmptyJsonAsNullable and
      (__ \ VirtualObjectKeys.CreationTimeKey).write[DateTime] and
      (__ \ VirtualObjectKeys.DeletionTimeKey).writeNullable[DateTime] and
      (__ \ VirtualObjectKeys.SenseCapabilityKey).writeNullable[SenseCapability] and
      (__ \ VirtualObjectKeys.ActuateCapabilityKey).writeNullable[ActuateCapability]
    )(unlift(VirtualObject.unapply _))

   val VirtualObjectFormat = OFormat(VirtualObjectReads, VirtualObjectWrites)

}
