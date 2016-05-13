package wings.model.virtual.operations

import play.api.libs.json.{Reads, JsValue, JsResult}

object VoOps extends Enumeration {

  type Op = Value

  val VoWatch = Value("vo/watch")

  val VoActuate = Value("vo/actuate")

}

object VoOp {

  implicit object VoOpReads extends Reads[VoOp] {
    override def reads(json: JsValue): JsResult[VoOp] = json.validate[VoWatch] orElse json.validate[VoActuate]
  }

}

trait VoOp {
  val operation: VoOps.Op

}
