/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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
  def files: List[ModelicaPath]
  def isLeaf: Boolean = !isNode
  def isNode: Boolean = !isLeaf
  def rename(name:String): FileTree
}

@JsonCodec
case class Leaf(override val path:Path, file: ModelicaPath) extends FileTree {
  override def files: List[ModelicaPath] = List(file)
  override def isLeaf: Boolean = true
  override def rename(name:String): FileTree = Leaf(Paths.get(name), file)
}
@JsonCodec
case class Node(override val path:Path, children:List[FileTree]) extends FileTree {
  override def files: List[ModelicaPath] = children.flatMap(_.files)
  override def isNode: Boolean = true
  override def rename(name:String): FileTree = Node(Paths.get(name), children)
}


object FileTree {
  def generate(
    filter: File => Boolean,
    fn: Path => ModelicaPath = ModelicaPath.apply,
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
