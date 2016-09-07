package wings.model.virtual.virtualobject.metadata


import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import wings.model.{HasIdentity, HasVoId, IdentityManager}
import wings.virtualobject.infrastructure.keys.VirtualObjectKeys

object VOMetadataIdentityManager extends IdentityManager[VOMetadata, UUID] {

  override def name: String = "_id"

  override def next: UUID = UUID.randomUUID()

  override def of(entity: VOMetadata): Option[UUID] = entity.id
}

case class VOMetadata(
                       id: Option[UUID],
                       voId: UUID,
                       hname: Option[String],
                       hdesc: Option[String],
                       states: Array[StateMetadata]
                     )
  extends HasIdentity[UUID] with HasVoId {

}

object VOMetadata {

  val HNameKey = "hname"
  val HDescKey = "hdesc"
  val StatesKey = "states"

  val virtualObjectMetadataReads: Reads[VOMetadata] = (
    (__ \ VOMetadataIdentityManager.name).readNullable[UUID] and
    (__ \ VirtualObjectKeys.VOIDKey).read[UUID] and
    (__ \ HNameKey).readNullable[String] and
    (__ \ HDescKey).readNullable[String] and
    (__ \ StatesKey).read[Array[StateMetadata]]
  )(VOMetadata.apply _)

  val virtualObjectMetadataWrites: OWrites[VOMetadata] = (
    (__ \ VOMetadataIdentityManager.name).writeNullable[UUID] and
      (__ \ VirtualObjectKeys.VOIDKey).write[UUID] and
      (__ \ HNameKey).writeNullable[String] and
      (__ \ HDescKey).writeNullable[String] and
      (__ \ StatesKey).write[Array[StateMetadata]]
    )(unlift(VOMetadata.unapply _))

  implicit val voMetadataFormat = OFormat(virtualObjectMetadataReads, virtualObjectMetadataWrites)

}
