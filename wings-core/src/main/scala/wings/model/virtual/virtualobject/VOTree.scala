package wings.model.virtual.virtualobject

import wings.collection.mutable.tree.Tree
import wings.virtualobject.domain.VirtualObject

import scala.collection.mutable.ListBuffer

case class VOTree(override val node: VirtualObject) extends Tree[VirtualObject] {
  override val children: ListBuffer[Tree[VirtualObject]] = ListBuffer[Tree[VirtualObject]]()
  override var parent: Option[Tree[VirtualObject]]       = None
}
