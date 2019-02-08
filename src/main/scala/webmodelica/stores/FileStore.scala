package webmodelica.stores

import com.twitter.util.Future
import webmodelica.models.{ModelicaFile, ModelicaFileDocument}

trait FileStore {
  def update(file:ModelicaFile): Future[Unit]
  def update(files:Seq[ModelicaFile]):Future[Unit] = {
    Future.join(files.map(update))
  }
  def updateDocuments(documents: Seq[ModelicaFileDocument]): Future[Unit] = {
    update(documents.map(ModelicaFile.apply))
  }
}
