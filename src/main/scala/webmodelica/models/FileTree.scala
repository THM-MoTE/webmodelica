package webmodelica.models

import java.nio.file.{
  Files,
  Path,
  Paths
}
import better.files._
import io.scalaland.chimney.dsl._
import io.circe.generic.JsonCodec
import webmodelica._

@JsonCodec
sealed trait FileTree {
  def path: Path
  def files: List[ModelicaFile]
  def isLeaf: Boolean = !isNode
  def isNode: Boolean = !isLeaf
}

@JsonCodec
case class Leaf(override val path:Path, file: ModelicaFile) extends FileTree {
  override def files: List[ModelicaFile] = List(file)
  override def isLeaf: Boolean = true
}
@JsonCodec
case class Node(override val path:Path, children:List[FileTree]) extends FileTree {
  override def files: List[ModelicaFile] = children.flatMap(_.files)
  override def isNode: Boolean = true
}


object FileTree {
  def baseMapper(path:Path): ModelicaFile =
    ModelicaFile(path, new String(Files.readAllBytes(path), constants.encoding))

  def generate(
    filter: File => Boolean,
    fn: Path => ModelicaFile = baseMapper,
    pathShortener: Path => Path = identity)(base:Path): FileTree = {
    //include directories to recurse into subdirectories
    val includeDirFiler = (f:File) => f.isDirectory || filter(f)
    def rec(current: File): FileTree = {
      val p = current.path
      if(current.isDirectory) {
        //don't know why, but we need to filter on the iterable, not on the File#list(filter) generator
        //File#list runs into a stackoverflow..
        val childs = current.list.filter(includeDirFiler).toList.map(rec).sortWith { (a,b) =>
          //sorts the entries:
          //first directories lexicographically sorted
          //then files lexicographically sorted
          if(a.isNode && b.isLeaf) true //if a is node and b isn't: a is always greater
          else (a.path compareTo b.path) < 1
        }
        Node(pathShortener(p), childs)
      } else {
        Leaf(pathShortener(p), fn(p))
      }
    }
    rec(File(base.toString))
  }
}
