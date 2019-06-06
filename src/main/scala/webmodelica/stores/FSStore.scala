/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.stores

import better.files._
import better.files.Dsl._
import java.nio.file.Path
import webmodelica.models._
import webmodelica.models.mope.requests._
import com.twitter.util.Future
import com.twitter.finatra.http.exceptions.NotFoundException

class FSStore(root:Path)
    extends FileStore
    with com.twitter.inject.Logging {
  import webmodelica.constants.encoding

  mkdirs(File(root))

  def rootDir: Path = root
  def update(file:ModelicaFile): Future[Unit] = {
    val fd = File(root.resolve(file.relativePath))
    mkdirs(fd/`..`)
    fd.createIfNotExists()
      .write(file.content)(charset=encoding)
    Future.value(())
  }

  def delete(p:Path): Future[Unit] = Future { File(root.resolve(p)).delete(swallowIOExceptions=true) }
  def rename(oldPath:Path, newPath:Path): Future[ModelicaPath] = {
      val newFile = File(root.resolve(newPath))
      val file = File(root.resolve(oldPath))
      if(file.exists && !newFile.exists) {
        file.renameTo(newFile.toString)
        Future.value(ModelicaPath(root.relativize(newFile)))
      } else if (newFile.exists) {
        Future.exception(errors.FileAlreadyInUse(newPath.toString))
      } else {
        Future.exception(NotFoundException(s"$oldPath not found!"))
      }
    }

  override def files: Future[List[ModelicaPath]] = {
    Future.value(
      File(root)
        .glob("**.mo")
        .map(f => ModelicaPath(root.relativize(f.path)))
        .toList
        .sortBy(_.relativePath)
    )
  }

  override def packageProjectArchive(name:String): Future[java.io.File] = {
    import scala.sys.process._
    val outDir = File(rootDir) / ProjectDescription(rootDir.toString).outputDirectory
    //package up all files inside of the project root, except the output directory
    val zipFile = File(s"/tmp/${name}.zip")
    zipFile.delete(swallowIOExceptions = true)
    val args = Seq("zip", "-r", s"--exclude=*${outDir.name}*", "--exclude=*.zip", zipFile.toString, "./"+rootDir.getFileName.toString)
    //run zip in parent directory of the project
    val process = Process(args, rootDir.getParent.toFile)
    debug(s"running $args in ${rootDir.getParent}")
    Future(process.!).flatMap {
      case status if status==0 => Future.value(zipFile.toJava)
      case code => Future.exception(errors.ArchiveError(s"Zipping $name failed, exit code: $code!"))
    }
  }

  override def copyTo(destination:Path): Future[Unit] = Future {
    val dest = File(destination.toString)
    File(rootDir.toString).copyTo(dest)
  }


  val treeGenerator = {
    val fileFilter = (f:File) => f.name.endsWith(".mo")
    val mapper = (p:Path) => ModelicaPath(root.relativize(p))
    val shortener = (p:Path) => p.getFileName
    FileTree.generate(fileFilter, mapper, shortener)(_)
  }
  override def fileTree(projectName:Option[String]=None): Future[FileTree] = Future {
    val tree = treeGenerator(root)
    projectName match {
      case Some(name) => tree.rename(name)
      case None => tree
    }
  }

  override def findByPath(p:Path): Future[Option[ModelicaFile]] = Future {
    val path = root.resolve(p)
    val file = File(path)
    if(file.exists) Some(ModelicaFile(root.relativize(path),file.contentAsString(charset=encoding)))
    else None
  }

  override def toString:String = s"FSStore($root)"
}

object FSStore {
  import webmodelica.constants.encoding
  /** Searches for the file containing the given 'model' name.
   *  Currently it traverses all files in the root directory and greps for a `model|class <modelname>` definition.
  */
  def findFileFor(root:Path)(model:String): Future[Option[Path]] = Future {
    val pattern = s"""(?:(?:model)|(?:class))\\s+${model}""".r
    val optionalFile = File(root)
      .glob("**.mo")
      .map(f => ModelicaFile(f.path, f.contentAsString(charset=encoding)))
      .find(f => pattern.findFirstIn(f.content).isDefined)
    optionalFile.map(_.relativePath)
  }
}
