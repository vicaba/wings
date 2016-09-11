package wings.virtualobject.domain

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class ActuateCapability(name: String, states: Array[ActuateState])
