package wings.model

trait ActorReferenced {
  val actorRef: Option[String]
}

object ActorReferenced {
  val ReferenceKey = "actorRef"
}
