package webmodelica.controllers

import com.google.inject.Inject
import com.twitter.util.{Future, FuturePool}
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.fileupload._
import org.mongodb.scala.bson.BsonObjectId
import webmodelica.models._
import webmodelica.models.mope.{FilePath, FilePosition}
import webmodelica.models.mope.requests.{Complete, SimulateRequest}
import webmodelica.models.mope.responses.{SimulationResult, Suggestion}
import webmodelica.services.{SessionRegistry, SessionService, TokenGenerator, TokenValidator}
import webmodelica.stores.{ProjectStore, UserStore}
import com.fasterxml.jackson.annotation.JsonProperty
import com.twitter.finatra.request._
import io.scalaland.chimney.dsl._
import java.nio.file.{Files, Path, Paths}

import better.files._

case class NewFileRequest(
  @RouteParam() sessionId: String,
  @JsonProperty() relativePath: java.nio.file.Path,
  @JsonProperty() content: String,
)
case class DeleteRequest(
  @RouteParam() sessionId: String,
  @QueryParam() path:String
)
case class RenameRequest(
  @RouteParam() sessionId: String,
  @JsonProperty() oldPath:Path,
  @JsonProperty() newPath:Path,
)
case class CompileRequest(
  @RouteParam() sessionId: String,
  @JsonProperty() path: java.nio.file.Path,
)
case class CompleteRequest(
  @RouteParam() sessionId: String,
  @JsonProperty() file: String,
  @JsonProperty() position: FilePosition,
  @JsonProperty() word: String
) {
  def toMopeRequest: Complete =
    this.into[Complete].transform
}
case class FSimulateRequest(
  @RouteParam() sessionId: String,
  @JsonProperty() modelName: String,
  @JsonProperty() options: Map[String, Any],
  request: Request,
) {
  def toMopeRequest: SimulateRequest = {
    import io.circe.syntax._
    val opts = options.mapValues {
      case v:Long => v.asJson
      case v:Int => v.asJson
      case v:Double => v.asJson
      case v:String => v.asJson
    }
    this.into[SimulateRequest].withFieldComputed(_.options, _ => opts).transform
  }
}

case class SimulationResponse(location: java.net.URI)
case class FSimulateStatusRequest(
  @RouteParam() sessionId: String,
  @QueryParam format:String="default",
  @QueryParam addr:java.net.URI
)
case class TableFormat(
  modelName: String,
  data: Traversable[Traversable[Double]],
  header: Traversable[String]
)

class SessionController@Inject()(
  projectStore:ProjectStore,
  sessionRegistry: SessionRegistry,
  prefix:webmodelica.ApiPrefix,
  override val userStore: UserStore,
  override val gen: TokenValidator)
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
          project <- projectStore.findBy(id, t.username).flatMap(errors.notFoundExc(s"project with $id not found!"))
          (service, session) <- FuturePool.unboundedPool(sessionRegistry.create(project))
          files <- service.files
        } yield {
          service.connect()
          JSSession(session, files)
        }
      }
      delete("/sessions/:sessionId") { req: Request =>
        sessionRegistry.killSession(req.getParam("sessionId")).map(_ => response.noContent)
      }
      post("/sessions/:sessionId/files/update") { req: NewFileRequest =>
        withSession(req.sessionId) { service =>
          service.update(ModelicaFile(req.relativePath, req.content))
        }
      }

      post("/sessions/:sessionId/files/upload") { req: Request =>
        withSession(req.getParam("sessionId")) { service =>
          val uploadRequ = new FinagleRequestFileUpload()
          uploadRequ.parseMultipartItems(req).get("archive").map { archive =>
            val p = Paths.get("/tmp", archive.filename.get)
            File(p).writeByteArray(archive.data)
            service.extractArchive(p)
          }
            .getOrElse(Future.value(response.badRequest.body("'archive' file expected!")))
        }
      }

      delete("/sessions/:sessionId/files") { req: DeleteRequest =>
        withSession(req.sessionId) { service =>
          service.delete(Paths.get(req.path)).map(_ => response.noContent)
        }
      }
      put("/sessions/:sessionId/files/rename") { req: RenameRequest =>
        withSession(req.sessionId) { service =>
          service.rename(req.oldPath, req.newPath)
        }
      }

      post("/sessions/:sessionId/compile") { req: CompileRequest =>
        withSession(req.sessionId) { service =>
          service.compile(req.path)
        }
      }
      post("/sessions/:sessionId/complete") { req: CompleteRequest =>
        withSession(req.sessionId) { service =>
          service.complete(req.toMopeRequest)
        }
      }
      post("/sessions/:sessionId/simulate") { req: FSimulateRequest =>
        withSession(req.sessionId) { service =>
          service.simulate(req.toMopeRequest).map { uri =>
            val location = req.request.uri.toString+s"?addr=${uri.toString}"
            response
              .ok(SimulationResponse(new java.net.URI(location)))
              .location(location)
          }.handle {
            case e:errors.StepSizeCalculationError =>
              response.badRequest(e.getMessage)
            }
        }
      }
      get("/sessions/:sessionId/simulate") { req: FSimulateStatusRequest =>
        withSession(req.sessionId) { service =>
          service
            .simulationResults(req.addr)
            .flatMap {
              case SimulationResult(name, variables) if req.format=="csv" =>
                service.locateSimulationCsv(name)
                  .map {
                    case Some(file) => sendFile(response)("text/csv", file)
                    case None => response.notFound(s"no results for $name available!")
                  }
              case SimulationResult(name, variables) if req.format == "chartjs" =>
                val headers = variables.keys.filterNot(k => k=="time").toList
                val tableData = (variables("time")+:headers.map(k => variables(k)).toSeq).transpose
                Future.value(TableFormat(name, tableData, "time"::headers))
              case results => Future.value(results)
            }
        }
      }
    }
}
