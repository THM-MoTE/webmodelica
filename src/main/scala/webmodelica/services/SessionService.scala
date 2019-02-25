package webmodelica.services

import com.google.inject.Inject
import com.twitter.finagle.Service
import com.twitter.finagle.Http
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.json.FinatraObjectMapper
import com.twitter.util.{Future, Time}
import webmodelica.models.config.MopeClientConfig
import webmodelica.models.{ModelicaFile, Session}
import webmodelica.stores.{FSStore, FileStore}
import java.nio.file._

import scala.concurrent.{ Future => SFuture, Promise =>  SPromise }
import scala.concurrent.ExecutionContext.Implicits.global

class SessionService @Inject()(
  val conf:MopeClientConfig,
  val session:Session)
  extends FileStore
    with MopeService
  with com.twitter.inject.Logging
  with com.twitter.util.Closable {
  override def clientProvider() = new featherbed.Client(new java.net.URL(conf.address+"mope/"))


  val fsStore = new FSStore(conf.data.hostDirectory.resolve(session.basePath))

  override val pathMapper = MopeService.pathMapper(fsStore.rootDir.toAbsolutePath, conf.data.bindDirectory.resolve(fsStore.rootDir.toAbsolutePath.getFileName()))

  info(s"mapper: $pathMapper")
  info(s"fsStore: $fsStore")
  override def rootDir: Path = fsStore.rootDir
  override def update(file: ModelicaFile): Future[Unit] = fsStore.update(file)
  override def files: Future[List[ModelicaFile]] = fsStore.files

  override def close(deadline:Time):Future[Unit] = disconnect()
}
