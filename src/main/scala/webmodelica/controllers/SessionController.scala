package webmodelica.controllers

import com.google.inject.Inject
import com.twitter.util.Future
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import org.mongodb.scala.bson.BsonObjectId
import webmodelica.models._
import webmodelica.services.SessionRegistry
import webmodelica.stores.ProjectStore
import com.fasterxml.jackson.annotation.JsonProperty
import com.twitter.finatra.request._

case class NewFileRequest(
  @RouteParam() sessionId: String,
  @JsonProperty() path: java.nio.file.Path,
  @JsonProperty() content: String,
)

class SessionController@Inject()(projectStore:ProjectStore, sessionRegistry: SessionRegistry)
  extends Controller {

  post("/projects/:projectId/sessions/new") { requ:Request =>
    val id = requ.getParam("projectId")
    for {
      project <- projectStore.findBy(BsonObjectId(id))
      _ = require(project ne null, "searched project can't be null!")
    } yield JSSession(sessionRegistry.create(project))
  }

  post("/sessions/:sessionId/files/update") { req:NewFileRequest =>
    sessionRegistry.get(req.sessionId) match {
      case Some(service) => service.update(ModelicaFile(req.path,req.content))
          case None => response.notFound.body(s"Can't find a session for: $req.sessionId")
    }
  }
}
