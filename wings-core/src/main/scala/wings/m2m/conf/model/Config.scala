package wings.m2m.conf.model

import play.api.libs.json._
import play.api.libs.json.Reads._

import scala.util.Try

/**
 * Config companion object
 */
object Config {

  type ValueType = String

  val ActionKey = "action"

  val ValueKey = "value"

  object Action extends Enumeration {

    type Action = Value

    val nameAcquisitionRequest = Value("nameAcquisitionRequest")
    val nameAcquisitionReject = Value("nameAcquisitionReject")
    val nameAcquisitionAck = Value("nameAcquisitionAck")

    val broadcast = Value("broadcast")

  }

  implicit object ConfigReads extends Reads[Config] {

    def hTypeCast(action: Config.Action.Value, value: Config.ValueType): Config = {
      action match {
        case Config.Action.nameAcquisitionRequest => NameAcquisitionRequest(value)
        case Config.Action.nameAcquisitionReject => NameAcquisitionReject(value)
        case Config.Action.nameAcquisitionAck => NameAcquisitionAck(value)
      }
    }

    override def reads(json: JsValue): JsResult[Config] = json match {
      case json: JsObject =>

       (for {
          actionString <- (json \ ActionKey).asOpt[String]
          action <- Try(Config.Action.withName(actionString)).toOption
          value <- (json \ ValueKey).asOpt[String]
        } yield {
          JsSuccess(hTypeCast(action, value))
        }).getOrElse(JsError("Can't convert to Config"))
      case _ => JsError("Can't convert to Config")
    }
  }

  implicit object ConfigWrites extends OWrites[Config] {

    def jsObjectCreator(action: Config.Action.Value, value: Config.ValueType): JsObject = {
      Json.obj(ActionKey -> action.toString, ValueKey -> Json.toJson(value))
    }

    override def writes(o: Config): JsObject = o match {
      case c: NameAcquisitionRequest => jsObjectCreator(Config.Action.nameAcquisitionRequest, c.value)
      case c: NameAcquisitionReject => jsObjectCreator(Config.Action.nameAcquisitionReject, c.value)
      case c: NameAcquisitionAck => jsObjectCreator(Config.Action.nameAcquisitionAck, c.value)
    }
  }
}

sealed trait Config {
  val value: Config.ValueType
}

/**
 * Intermediate config message
 * @param value
 */
case class NameAcquisitionRequest(override val value: String)
  extends Config

case class NameAcquisitionReject(override val value: String)
  extends Config

case class NameAcquisitionAck(override val value: String)
  extends Config

case class Broadcast(override val value: String)
  extends Config




