package webmodelica.controllers

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import webmodelica.models.config.WMConfig
import com.google.inject.Inject

class Simple @Inject()(config: WMConfig)
    extends Controller {

  case class PingResponse(msg:String, agent:String, config:WMConfig)

  get("/ping") { request: Request =>
    PingResponse("loaded config", request.userAgent.get, config)
  }
}
