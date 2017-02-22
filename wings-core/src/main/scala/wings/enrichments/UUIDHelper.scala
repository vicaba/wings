package wings.enrichments

import java.nio.ByteBuffer
import java.util.{Base64, UUID}

import scala.util.Try

object UUIDHelper {

  def tryFromString(s: String): Try[UUID] = {
    Try(UUID.fromString(s))
  }

  implicit class UUIDEnrichment(u: UUID) {

    def copy: UUID = {
      UUID.fromString(u.toString)
    }

    def toBase64: String = {
      val encoder               = Base64.getUrlEncoder
      val uuidBytes: ByteBuffer = ByteBuffer.allocate(16)
      uuidBytes.putLong(u.getMostSignificantBits)
      uuidBytes.putLong(u.getLeastSignificantBits)
      encoder.encode(uuidBytes.array()).toString
    }
  }
}
