package wings.virtualobjectagent.domain.messages

trait VirtualObjectOperations

object VirtualObjectOperations extends Enumeration {

  type Op = Value

  val Watch = Value("vo/watch")

  val Actuate = Value("vo/actuate")

}
