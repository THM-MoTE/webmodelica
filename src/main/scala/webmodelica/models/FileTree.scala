package webmodelica.models

import java.nio.file.{
  Files,
  Path
}
import better.files._
import io.scalaland.chimney.dsl._
import io.circe.generic.JsonCodec
import webmodelica._

@JsonCodec
sealed trait FileTree {
  def path: Path
}

@JsonCodec
case class Leaf(override val path:Path, file: ModelicaFile) extends FileTree
@JsonCodec
case class Node(override val path:Path, children:List[FileTree]) extends FileTree

object FileTree {
  def baseMapper(path:Path): ModelicaFile =
    ModelicaFile(path, new String(Files.readAllBytes(path), constants.encoding))

  def generate(base:Path)(fn: Path => ModelicaFile = baseMapper): FileTree = {

  }
}
