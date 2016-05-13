package wings.model.virtual.virtualobject

import wings.collection.mutable.tree.Tree

import scala.collection.mutable.ListBuffer

case class VOTree(override val node: VO) extends Tree[VO] {
  override val children: ListBuffer[Tree[VO]] = ListBuffer[Tree[VO]]()
  override var parent: Option[Tree[VO]] = None
}
