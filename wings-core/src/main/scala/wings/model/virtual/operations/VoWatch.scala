package wings.model.virtual.operations

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class VoWatch(override val operation: VoOps.Op = VoOps.VoWatch, path: String) extends VoOp

object VoWatch {
  val voWatchReads: Reads[VoWatch] = (
    (__ \ "op").read[String].filter(op => op == VoOps.VoWatch.toString).map(op => VoOps.VoWatch) and
      (__ \ "path").read[String]
    ) (VoWatch.apply _)

  val voWatchWrites: OWrites[VoWatch] = (
    (__ \ "op").write[VoOps.Op] and
      (__ \ "path").write[String]
    ) (unlift(VoWatch.unapply _))


  implicit val voWatchFormat = OFormat(voWatchReads, voWatchWrites)
}