package wings.model.virtual.operations

import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
  * Created by vicaba on 08/01/16.
  */
case class VoActuate(override val operation: VoOps.Op = VoOps.VoActuate, path: String, stateId: String) extends VoOp

object VoActuate {
  val voActuateReads: Reads[VoActuate] = (
    (__ \ "op").read[String].filter(op => op == VoOps.VoActuate.toString).map(op => VoOps.VoActuate) and
      (__ \ "path").read[String] and
        (__ \ "stateId").read[String]
    ) (VoActuate.apply _)

  val voActuateWrites: OWrites[VoActuate] = (
    (__ \ "op").write[VoOps.Op] and
      (__ \ "path").write[String] and
      (__ \ "stateId").write[String]
    ) (unlift(VoActuate.unapply _))


  implicit val voActuateFormat = OFormat(voActuateReads, voActuateWrites)
}

object Main {
  def main(args: Array[String]) {
    val stringMsg = """{"op": "vo/actuate", "path": "73f86a2e-1004-4011-8a8f-3f78cdd6113c", "stateId": "on"}"""
    val jsonMsg = Json.parse(stringMsg)
    val actuate = jsonMsg.validate[VoActuate]
    println(actuate)

  }
}