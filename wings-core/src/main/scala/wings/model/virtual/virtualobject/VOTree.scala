package wings.model.virtual.virtualobject

import scala.collection.mutable.ListBuffer

import wings.collection.mutable.tree.Tree
import wings.virtualobject.domain.VirtualObject

case class VOTree(override val node: VirtualObject) extends Tree[VirtualObject] {
  override val children: ListBuffer[Tree[VirtualObject]] = ListBuffer[Tree[VirtualObject]]()
  override var parent: Option[Tree[VirtualObject]]       = None
}
