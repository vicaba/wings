package wings.collection.mutable.tree

import scala.collection.mutable.ListBuffer

trait TreeLike[Container[X], Node] {

  def add(n: Node): Container[Node]
  def getWhere(predicate: (Node) => Boolean): Option[Container[Node]]
  def deleteWhere(predicate: (Node) => Boolean): Option[Container[Node]]
  def size: Int
}

trait Tree[Node] extends TreeLike[Tree, Node] {

  val node: Node

  var parent: Option[Tree[Node]]

  val children: ListBuffer[Tree[Node]]

  protected def newNode(n: Node): Tree[Node] = new Tree[Node] {
    var parent: Option[Tree[Node]] = Some(Tree.this)
    val node                       = n
    lazy val children              = ListBuffer[Tree[Node]]()
  }

  def add(n: Node): Tree[Node] = {
    val _newNode = newNode(n)
    children += _newNode
    _newNode
  }

  def deleteWhere(predicate: (Node) => Boolean): Option[Tree[Node]] = {
    var deleted: Option[Tree[Node]] = None
    getWhere(predicate).foreach { n =>
      n.parent.foreach { p =>
        val children = p.children
        val length   = children.length
        for (i <- 0 until length) {
          val currentNode = children(i)
          if (currentNode.node == n.node) {
            children.remove(i)
            n.parent = None
            deleted = Some(n)
          }
        }
      }
    }
    deleted
  }

  private def traverse(tree: Tree[Node]): Stream[Tree[Node]] =
    tree #:: (tree.children map traverse).fold(Stream.Empty)(_ ++ _)

  def size: Int = {
    traverse(this).length
  }

  def getWhere(predicate: (Node) => Boolean): Option[Tree[Node]] = traverse(this) find { tree =>
    predicate(tree.node)
  }

  override def toString = {
    s"""[$node${if (children.isEmpty) "" else s", Children: $children"}]"""
  }
}

object Main {

  case class ConcreteTree(override val node: Int) extends Tree[Int] {

    override val children = ListBuffer[Tree[Int]]()

    override var parent: Option[Tree[Int]] = None

  }

  def main(args: Array[String]) {
    val tree1 = ConcreteTree(1)
    val tree2 = tree1.add(2)
    val tree3 = tree1.add(3)

    println(tree1.getWhere(_ == 1))
    println(tree1.size)

    val tree4 = tree1.add(8)

    println(tree1.getWhere(_ == 8).map(_.add(9)))
    println(tree1.size)

    tree1.deleteWhere(_ == 8)

    println(s"printing tree: $tree1")
  }
}
