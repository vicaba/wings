package wings.model.virtual.virtualobject

import java.time.ZonedDateTime
import java.util.UUID

import wings.model.virtual.virtualobject.actuate.ActuateCapability
import wings.model.virtual.virtualobject.sense.SenseCapability
import wings.model.{HasVoId, ActorReferenced, HasIdentity, IdentityManager}

import play.api.libs.json._

// JSON library

import play.api.libs.json.Reads._

// Custom validation helpers

import play.api.libs.functional.syntax._

// Combinator syntax

object VOIdentityManager extends IdentityManager[VO, UUID] {

  override def name: String = "_id"

  override def next: UUID = UUID.randomUUID()

  override def of(entity: VO): Option[UUID] = entity.id
}

case class VO(
               override val id: Option[UUID],
               override val voId: UUID,
               pVoId: Option[UUID],
               actorRef: Option[String],
               children: Option[Array[String]],
               path: String,
               metadata: Option[UUID],
               creationTime: ZonedDateTime,
               deletionTime: Option[ZonedDateTime],
               senseCapability: Option[SenseCapability],
               actuateCapability: Option[ActuateCapability]
             ) extends HasIdentity[UUID] with HasVoId

object VO {

  type Id = UUID

  val VOIDKey = "VOID"
  val PVOIDKey = "pVOID"
  val ChildrenKey = "children"
  val PathKey = "path"
  val MetadataKey = "metadata"
  val CreationTimeKey = "c"
  val DeletionTimeKey = "d"
  val SenseCapabilityKey = "scap"
  val ActuateCapabilityKey = "acap"

  val vOReads: Reads[VO] = (
      (__ \ VOIdentityManager.name).readNullable[UUID] and
      (__ \ VOIDKey).read[UUID] and
      (__ \ PVOIDKey).readNullable[UUID] and
      (__ \ ActorReferenced.ReferenceKey).readNullable[String] and
      (__ \ ChildrenKey).readNullable[Array[String]] and
      (__ \ PathKey).read[String] and
      (__ \ MetadataKey).readNullable[UUID] and
      (__ \ CreationTimeKey).read[ZonedDateTime] and
      (__ \ DeletionTimeKey).readNullable[ZonedDateTime] and
      (__ \ SenseCapabilityKey).readNullable[SenseCapability] and
      (__ \ ActuateCapabilityKey).readNullable[ActuateCapability]
    )(VO.apply _)

  val vOWrites: OWrites[VO] = (
      (__ \ VOIdentityManager.name).writeNullable[UUID] and
      (__ \ VOIDKey).write[UUID] and
      (__ \ PVOIDKey).writeNullable[UUID] and
      (__ \ ActorReferenced.ReferenceKey).writeNullable[String] and
      (__ \ ChildrenKey).writeNullable[Array[String]] and
      (__ \ PathKey).write[String] and
      (__ \ MetadataKey).writeNullable[UUID] and
      (__ \ CreationTimeKey).write[ZonedDateTime] and
      (__ \ DeletionTimeKey).writeNullable[ZonedDateTime] and
      (__ \ SenseCapabilityKey).writeNullable[SenseCapability] and
      (__ \ ActuateCapabilityKey).writeNullable[ActuateCapability]
    )(unlift(VO.unapply _))

  implicit val vOFormat = OFormat(vOReads, vOWrites)
}