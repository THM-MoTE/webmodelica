package webmodelica.stores

import com.twitter.util.Future
import webmodelica.models.{Session, Project, ModelicaFile, ModelicaFileDocument}
import java.nio.file.Path
import better.files._

trait FileStore {
  def rootDir: Path
  def files: Future[List[ModelicaFile]]
  def update(file:ModelicaFile): Future[Unit]
  def delete(p:Path): Future[Unit]
  def rename(oldPath:Path, newPath:Path): Future[ModelicaFile]
  def update(files:Seq[ModelicaFile]):Future[Unit] = {
    Future.join(files.map(update))
  }
  def updateDocuments(documents: Seq[ModelicaFileDocument]): Future[Unit] = {
    update(documents.map(ModelicaFile.apply))
  }
  def packageProjectArchive(name:String): Future[java.io.File]
  def copyTo(destination:Path): Future[Unit]
}


object FileStore {
  def fromProject(basePath:Path, project:Project): FileStore = fromSession(basePath, Session(project))
  def fromSession(basePath:Path, s:Session): FileStore = new FSStore(basePath resolve s.basePath)
}
