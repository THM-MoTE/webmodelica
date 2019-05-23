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
}

@JsonCodec
case class Leaf(override val path:Path, file: ModelicaFile) extends FileTree {
  override def files: List[ModelicaFile] = List(file)
}
@JsonCodec
case class Node(override val path:Path, children:List[FileTree]) extends FileTree {
  override def files: List[ModelicaFile] = children.flatMap(_.files)
}


object FileTree {
  def baseMapper(path:Path): ModelicaFile =
    ModelicaFile(path, new String(Files.readAllBytes(path), constants.encoding))

  def generate(filter: File => Boolean, fn: Path => ModelicaFile = baseMapper)(base:Path): FileTree = {
    def rec(base: File): FileTree = {
      val p = base.path
      if(base.isDirectory) {
        val childs = base.list(filter).map(rec)
        Node(p, childs.toList)
      } else {
        Leaf(p, fn(p))
      }
    }
    rec(File(base.toString))
  }
}
