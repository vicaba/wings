package wings.json

import play.api.data.validation.ValidationError
import play.api.libs.json._

import scala.util.Try

object Additions {


  object Formats {

    object UUID {

      object UUIDReads extends Reads[java.util.UUID] {
        def parseUUID(s: String): Option[java.util.UUID] = Try(java.util.UUID.fromString(s)).toOption

        def reads(json: JsValue) = json match {
          case JsString(s) =>
            parseUUID(s).map(JsSuccess(_)).getOrElse(JsError(Seq(JsPath() -> Seq(ValidationError("Expected UUID string")))))
          case _ =>
            JsError(Seq(JsPath() -> Seq(ValidationError("Expected UUID string"))))
        }
      }

      object UUIDWrites extends Writes[java.util.UUID] {
        def writes(uuid: java.util.UUID): JsValue = JsString(uuid.toString)
      }

      val uuidFormat: Format[java.util.UUID] = Format(UUIDReads, UUIDWrites)

    }

  }

  /**
   * Non functional nullable additions
   */
  object Nullable {

    /**
     * Writes this nullable iterable only if it is not empty
     * @param write the object to write
     * @param writes  an implicit writes for A
     * @tparam A  the iterable
     * @return  an empty JsonObject or a JsonObject containing String: A
     */
    def writeIterable[A <: Iterable[_]](write: (String, A))(implicit writes: Writes[A]) = {
      if (write._2.isEmpty) {
        Json.obj()
      } else {
        Json.obj(write._1 -> writes.writes(write._2))
      }
    }
  }

  /**
   * Implicit class grabbed from http://stackoverflow.com/questions/21297987/play-scala-how-to-prevent-json-serialization-of-empty-arrays, not tested
   * @param path  implicit path parameter
   */
  implicit class PathAdditions(path: JsPath) {

    def readNullableIterable[A <: Iterable[_]](implicit reads: Reads[A]): Reads[A] =
      Reads((json: JsValue) => path.applyTillLast(json).fold(
        error => error,
        result => result.fold(
          invalid = (_) => reads.reads(JsArray()),
          valid = {
            case JsNull => reads.reads(JsArray())
            case js => reads.reads(js).repath(path)
          })
      ))

    def writeNullableIterable[A <: Iterable[_]](implicit writes: Writes[A]): OWrites[A] =
      OWrites[A]{ (a: A) =>
      if (a.isEmpty) Json.obj()
      else JsPath.createObj(path -> writes.writes(a))
    }

    def lazyWriteNullableIterable[T <: Iterable[_]](w: => Writes[T]): OWrites[Option[T]] = OWrites((t: Option[T]) => {
      if(t != null) {
        t.getOrElse(Seq.empty).size match {
          case 0 => Json.obj()
          case _ => Writes.nullable[T](path)(w).writes(t)
        }
      }
      else {
        Json.obj()
      }
    })

    /** When writing it ignores the property when the collection is empty,
      * when reading undefined and empty jsarray becomes an empty collection */
    def formatNullableIterable[A <: Iterable[_]](implicit format: Format[A]): OFormat[A] =
      OFormat[A](r = readNullableIterable(format), w = writeNullableIterable(format))

    def writeEmptyJsonAsNullable(implicit writes: Writes[JsObject]): OWrites[JsObject] =
      OWrites[JsObject]{ jsObject =>
        if(jsObject.keys.isEmpty) Json.obj()
        else JsPath.createObj(path -> writes.writes(jsObject))
      }
  }
}
