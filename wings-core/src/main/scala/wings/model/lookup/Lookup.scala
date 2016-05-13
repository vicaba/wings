package wings.model.lookup

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

trait Lookup

trait ActorLookup extends Lookup

case class ActorSimpleLookup(name: String, actorPath: String) extends ActorLookup

object ActorSimpleLookupJSONImplicits {
  implicit val actorSimpleLookupReads: Reads[ActorSimpleLookup] = (
    (__ \ "name").read[String] and
    (__ \ "path").read[String]
  )(ActorSimpleLookup.apply _)

  implicit val actorSimpleLookUpWrites: OWrites[ActorSimpleLookup] = (
    (__ \ "name").write[String] and
    (__ \ "path").write[String]
   )(unlift(ActorSimpleLookup.unapply))
}


object JSONImplicits {


  implicit object LookupWrites extends OWrites[Lookup] {
    override def writes(o: Lookup): JsObject = {
      o match {
        case l: ActorSimpleLookup => ActorSimpleLookupJSONImplicits.actorSimpleLookUpWrites.writes(l)
      }
    }
  }
}