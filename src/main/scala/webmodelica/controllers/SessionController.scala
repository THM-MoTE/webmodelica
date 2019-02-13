package webmodelica.controllers

import com.google.inject.Inject
import com.twitter.util.{
  Future,
  FuturePool
}
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import org.mongodb.scala.bson.BsonObjectId
import webmodelica.models._
import webmodelica.services.{
  SessionRegistry,
  SessionService
}
import webmodelica.stores.ProjectStore
import com.fasterxml.jackson.annotation.JsonProperty
import com.twitter.finatra.request._

case class NewFileRequest(
  @RouteParam() sessionId: String,
  @JsonProperty() path: java.nio.file.Path,
  @JsonProperty() content: String,
)
case class CompileRequest(
  @RouteParam() sessionId: String,
  @JsonProperty() path: java.nio.file.Path,
)

class SessionController@Inject()(projectStore:ProjectStore, sessionRegistry: SessionRegistry)
  extends Controller {

  def withSession[A](id:webmodelica.UUIDStr)(fn: SessionService => Future[A]): Future[_] =
    FuturePool.unboundedPool(sessionRegistry.get(id)).flatMap {
      case Some(service) => fn(service)
      case None => Future.value(response.notFound.body(s"Can't find a session for: $id"))
    }

  post("/projects/:projectId/sessions/new") { requ:Request =>
    val id = requ.getParam("projectId")
    for {
      project <- projectStore.findBy(BsonObjectId(id)).flatMap(errors.notFoundExc(s"project with $id not found!"))
      _ = require(project ne null, "searched project can't be null!")
      session <- FuturePool.unboundedPool(sessionRegistry.create(project))
    } yield {
      withSession(session.idString)(_.connect())
      JSSession(session)
    }
  }

  post("/sessions/:sessionId/files/update") { req:NewFileRequest =>
    withSession(req.sessionId) { service =>
      service.update(ModelicaFile(req.path,req.content))
    }
  }

  post("/sessions/:sessionId/compile")  { req:CompileRequest =>
    withSession(req.sessionId) { service =>
      service.compile(req.path)
    }
  }
}
