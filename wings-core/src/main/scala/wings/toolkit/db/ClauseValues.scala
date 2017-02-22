package wings.toolkit.db

object ClauseValues {

  object SortOrder {

    sealed trait SortOrder

    case object Ascendant extends SortOrder

    case object Descendant extends SortOrder

    case class SortOrderWithKey(key: String, sortOrder: SortOrder)

  }

}
