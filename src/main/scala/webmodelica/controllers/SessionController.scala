package webmodelica.controllers

import com.google.inject.Inject
import com.twitter.util.{Future, FuturePool}
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import org.mongodb.scala.bson.BsonObjectId
import webmodelica.models._
import webmodelica.services.{TokenGenerator, SessionRegistry, SessionService}
import webmodelica.stores.{
  UserStore,
  ProjectStore
}
import com.fasterxml.jackson.annotation.JsonProperty
import com.twitter.finatra.request._

case class NewFileRequest(
  @RouteParam() sessionId: String,
  @JsonProperty() relativePath: java.nio.file.Path,
  @JsonProperty() content: String,
)
case class CompileRequest(
  @RouteParam() sessionId: String,
  @JsonProperty() path: java.nio.file.Path,
)

class SessionController@Inject()(
  projectStore:ProjectStore,
  sessionRegistry: SessionRegistry,
  prefix:webmodelica.ApiPrefix,
  override val userStore: UserStore,
  override val gen: TokenGenerator)
    extends Controller
    with UserExtractor {

  def withSession[A](id:webmodelica.UUIDStr)(fn: SessionService => Future[A]): Future[_] =
    FuturePool.unboundedPool(sessionRegistry.get(id)).flatMap {
      case Some(service) => fn(service)
      case None => Future.value(response.notFound.body(s"Can't find a session for: $id"))
    }

  filter[JwtFilter]
    .prefix(prefix.p) {
      post("/projects/:projectId/sessions/new") { requ: Request =>
        val id = requ.getParam("projectId")
        for {
          t <- extractToken(requ)
          project <- projectStore.findBy(BsonObjectId(id), t.username).flatMap(errors.notFoundExc(s"project with $id not found!"))
          (service, session) <- FuturePool.unboundedPool(sessionRegistry.create(project))
          files <- service.files
        } yield {
          service.connect()
          JSSession(session, files)
        }
      }
      post("/sessions/:sessionId/files/update") { req: NewFileRequest =>
        withSession(req.sessionId) { service =>
          service.update(ModelicaFile(req.relativePath, req.content))
        }
      }
      post("/sessions/:sessionId/compile") { req: CompileRequest =>
        withSession(req.sessionId) { service =>
          service.compile(req.path)
        }
      }
    }
}
