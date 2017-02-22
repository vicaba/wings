package wings.virtualobjectagent.infrastructure.messages.serialization.json

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsObject, Json, OFormat, OWrites, Reads, _}

import wings.toolkit.json.Additions._
import wings.virtualobject.domain.{ActuateCapability, SenseCapability}
import wings.virtualobject.infrastructure.keys.VirtualObjectKeys
import wings.virtualobject.infrastructure.serialization.json.Implicits._
import wings.virtualobjectagent.domain.messages.command.VirtualObjectBasicDefinition
import wings.virtualobjectagent.infrastructure.messages.keys.VirtualObjectBasicDefinitionKeys

object VirtualObjectBasicDefinitionJson {

  val VirtualObjectBasicDefinitionReads: Reads[VirtualObjectBasicDefinition] = (
    (__ \ VirtualObjectBasicDefinitionKeys.IdKey).read[UUID] and
      (__ \ VirtualObjectBasicDefinitionKeys.ParentIdKey).readNullable[UUID] and
      (__ \ VirtualObjectBasicDefinitionKeys.ChildrenKey).readNullable[Array[String]] and
      (__ \ VirtualObjectKeys.PathKey).readNullable[String] and
      (__ \ VirtualObjectBasicDefinitionKeys.MetadataKey).readNullable[JsObject].map(_.getOrElse(Json.obj())) and
      (__ \ VirtualObjectBasicDefinitionKeys.SenseCapabilityKey).readNullable[SenseCapability] and
      (__ \ VirtualObjectBasicDefinitionKeys.ActuateCapabilityKey).readNullable[ActuateCapability]
  )(VirtualObjectBasicDefinition.apply _)

  val VirtualObjectBasicDefinitionWrites: OWrites[VirtualObjectBasicDefinition] = (
    (__ \ VirtualObjectBasicDefinitionKeys.IdKey).write[UUID] and
      (__ \ VirtualObjectBasicDefinitionKeys.ParentIdKey).writeNullable[UUID] and
      (__ \ VirtualObjectBasicDefinitionKeys.ChildrenKey).writeNullable[Array[String]] and
      (__ \ VirtualObjectKeys.PathKey).writeNullable[String] and
      (__ \ VirtualObjectBasicDefinitionKeys.MetadataKey).writeEmptyJsonAsNullable and
      (__ \ VirtualObjectBasicDefinitionKeys.SenseCapabilityKey).writeNullable[SenseCapability] and
      (__ \ VirtualObjectBasicDefinitionKeys.ActuateCapabilityKey).writeNullable[ActuateCapability]
  )(unlift(VirtualObjectBasicDefinition.unapply _))

  val VirtualObjectBasicDefinitionFormat =
    OFormat(VirtualObjectBasicDefinitionReads, VirtualObjectBasicDefinitionWrites)

}
