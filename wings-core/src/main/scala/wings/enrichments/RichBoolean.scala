package wings.enrichments

object RichBoolean {

  implicit class RichBoolean(x: Boolean) {
    def isTrue[U](default: U)(f: () => U): U = if (x) f() else default
    def isFalse[U](default: U)(f: () => U): U = if (!x) f() else default
    def fold[U](isTrue: () => U, isFalse: () => U): U = if (x) isTrue() else isFalse()
  }
}
