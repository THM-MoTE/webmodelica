package webmodelica.services

import com.google.inject.Inject
import com.twitter.finagle.Service
import com.twitter.finagle.Http
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.json.FinatraObjectMapper
import com.twitter.util.Future
import webmodelica.models.config.MopeClientConfig
import webmodelica.models.{ModelicaFile, Session}
import webmodelica.stores.{FSStore, FileStore}
import java.nio.file._

class SessionService @Inject()(
  val conf:MopeClientConfig,
  val session:Session,
override val json:FinatraObjectMapper)
  extends FileStore
    with MopeService
  with com.twitter.inject.Logging {
  override lazy val client = new featherbed.Client(new java.net.URL(conf.address+"mope/"))

  val fsStore = new FSStore(conf.data.hostDirectory.resolve(session.id.toString))

  override val pathMapper = new PathMapper() {
    val hostPath = fsStore.rootDir.toAbsolutePath
    val bindPath = conf.data.bindDirectory.resolve(hostPath.getFileName())
    private val stripPath = (from:Path, other:Path) => from.subpath(other.getNameCount, from.getNameCount)
    override def relativize(p:Path): Path =
      if(p.startsWith(hostPath)) stripPath(p, hostPath)
      else stripPath(p, bindPath)
    override def toBindPath(p:Path): Path =
      if(p.isAbsolute) bindPath.resolve(stripPath(p, hostPath))
      else bindPath.resolve(p)
    override def toHostPath(p:Path): Path =
      if(p.isAbsolute) hostPath.resolve(stripPath(p, bindPath))
      else hostPath.resolve(p)
  }

  override def rootDir: Path = fsStore.rootDir
  override def update(file: ModelicaFile): Future[Unit] = fsStore.update(file)
}
