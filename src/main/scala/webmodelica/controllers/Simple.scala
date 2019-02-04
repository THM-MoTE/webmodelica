package webmodelica.controllers

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

class Simple
    extends Controller {

  case class PingResponse(msg:String, agent:String)

  get("/api/ping") { request: Request =>
    PingResponse("pong", request.userAgent.get)
  }
}
