package webmodelica.services

import com.google.inject.Inject
import com.twitter.finagle.Service
import com.twitter.finagle.Http
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.json.FinatraObjectMapper
import webmodelica.models.config.MopeClientConfig
import webmodelica.stores.FSStore

class SessionService @Inject()(conf:MopeClientConfig, override val json:FinatraObjectMapper)
    extends MopeService {
  override val mope = Http.client.newService(conf.address)
  override val  baseUri = conf.address
  val fsStore = new FSStore(conf.data.hostDirectory)
}
