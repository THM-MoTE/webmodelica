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

class SessionService @Inject()(
  val conf:MopeClientConfig,
  val session:Session,
override val json:FinatraObjectMapper)
  extends FileStore
    with MopeService
  with com.twitter.inject.Logging {
  override lazy val client = new featherbed.Client(new java.net.URL(conf.address+"mope/"))

  val fsStore = new FSStore(conf.data.hostDirectory.resolve(session.id.toString))

  override def update(file: ModelicaFile): Future[Unit] = fsStore.update(file)
}
