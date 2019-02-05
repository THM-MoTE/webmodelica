package webmodelica.controllers

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import webmodelica.models.config.WMConfig
import webmodelica.stores._
import webmodelica.models._
import com.google.inject.Inject
import org.mongodb.scala.bson.BsonObjectId

class Simple @Inject()(config: WMConfig, store:ProjectStore)
    extends Controller {

  case class PingResponse(msg:String, agent:String, config:WMConfig)

  get("/ping") { request: Request =>
    store.add(Project(BsonObjectId(), "nico", "proj title"))
    PingResponse("loaded config", request.userAgent.get, config)
  }
}
