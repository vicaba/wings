package wings.model

/**
 * Type class providing identity manipulation methods
 */

trait HasIdentity[ID] {
  val id: Option[ID]
}

trait IdentityManager[E <: HasIdentity[I], I] {
  def name: String
  def of(entity: E): Option[I]
  def next: I
  def nextOpt: Option[I] = Option(next)
}
