/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.stores

import com.twitter.util.Future
import webmodelica.models.{FileTree, ModelicaFile, ModelicaFileDocument, ModelicaPath, Project, Session}
import java.nio.file.Path

import better.files._

trait FileStore {
  def rootDir: Path
  def files: Future[List[ModelicaPath]]
  def update(file:ModelicaFile): Future[Unit]
  def delete(p:Path): Future[Unit]
  def rename(oldPath:Path, newPath:Path): Future[ModelicaPath]
  def update(files:Seq[ModelicaFile]):Future[Unit] = {
    Future.join(files.map(update))
  }
  def packageProjectArchive(name:String): Future[java.io.File]
  def copyTo(destination:Path): Future[Unit]
  def fileTree(projectName:Option[String]=None): Future[FileTree]
  def findByPath(p:Path): Future[Option[ModelicaFile]]
}


object FileStore {
  def fromProject(basePath:Path, project:Project): FileStore = fromSession(basePath, Session(project))
  def fromSession(basePath:Path, s:Session): FileStore = new FSStore(basePath resolve s.basePath)
}
