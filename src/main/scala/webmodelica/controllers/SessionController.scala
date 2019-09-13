/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.controllers

import com.google.inject.{
  Inject
}
import com.twitter.util.{Future, FuturePool}
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.fileupload._
import org.mongodb.scala.bson.BsonObjectId
import webmodelica.models._
import webmodelica.models.config.MaxSimulationData
import webmodelica.models.errors._
import webmodelica.models.mope.{FilePath, FilePosition}
import webmodelica.models.mope.requests.{Complete, SimulateRequest}
import webmodelica.models.mope.responses.{SimulationResult, Suggestion}
import webmodelica.services.{SessionRegistry, SessionService, TokenGenerator, TokenValidator}
import webmodelica.stores.{ProjectStore, UserStore}
import com.fasterxml.jackson.annotation.JsonProperty
import com.twitter.finatra.request._
import com.twitter.finatra.http.exceptions._
import io.scalaland.chimney.dsl._
import java.nio.file.{Files, Path, Paths}
import java.net.URI

import better.files._

object SessionController {

  case class NewFileRequest(
                             @RouteParam() sessionId: String,
                             @JsonProperty() relativePath: java.nio.file.Path,
                             @JsonProperty() content: String,
                           )

  case class DeleteRequest(
                            @RouteParam() sessionId: String,
                            @RouteParam() path: URI
                          ) {
    def asPath: Path = Paths.get(path.getPath)
  }

  case class RenameRequest(
                            @RouteParam() sessionId: String,
                            @JsonProperty() oldPath: Path,
                            @JsonProperty() newPath: Path,
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
        case v: Long => v.asJson
        case v: Int => v.asJson
        case v: Double => v.asJson
        case v: String => v.asJson
      }
      this.into[SimulateRequest].withFieldComputed(_.options, _ => opts).transform
    }
  }

  case class SimulationResponse(location: java.net.URI)

  case class FSimulateStatusRequest(
                                     @RouteParam() sessionId: String,
                                     @QueryParam format: String = "default",
                                     @QueryParam addr: java.net.URI,
                                     @QueryParam(commaSeparatedList = true) filter: Seq[String] = Seq()
                                   )

  case class TableFormat(
                          modelName: String,
                          data: Traversable[Traversable[Double]],
                          header: Traversable[String],
                          dataManipulated: Option[String]
                        )

}

class SessionController@Inject()(
  projectStore:ProjectStore,
  sessionRegistry: SessionRegistry,
  prefix:webmodelica.ApiPrefix,
  maxSimulationData:MaxSimulationData,
  override val userStore: UserStore,
  override val gen: TokenValidator)
    extends Controller
    with UserExtractor {

  info(s"maxSimulationdata: ${maxSimulationData.value}")

  def withSession[A](id:webmodelica.UUIDStr)(fn: SessionService => Future[A]): Future[_] =
    sessionRegistry.get(id).flatMap {
      case Some(service) => fn(service)
      case None => Future.exception(NotFoundException(s"Can't find a session for: $id"))
    }

  filter[JwtFilter]
    .prefix(prefix.p) {
      post("/projects/:id/sessions/new") { requ: Request =>
        val id = requ.getParam("id")
        for {
          t <- extractToken(requ)
          project <- projectStore.findBy(id, t.username).flatMap(errors.notFoundExc(s"project with $id not found!"))
          (service, session) <- sessionRegistry.create(project)
          files <- service.files
        } yield {
          //only connect if not already done: we did if we have an id
          if(session.mopeId.isEmpty) {
            service.connect()
          }
          JSSession(session, files)
        }
      }
      delete("/sessions/:sessionId") { req: Request =>
        sessionRegistry.killSession(req.getParam("sessionId")).map(_ => response.noContent)
      }
      post("/sessions/:sessionId/files/update") { req: SessionController.NewFileRequest =>
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
            .getOrElse(Future.exception(BadRequestException("'archive' file expected!")))
        }
      }
      delete("/sessions/:sessionId/files/:path") { req: SessionController.DeleteRequest =>
        withSession(req.sessionId) { service =>
          service.delete(req.asPath).map(_ => response.noContent)
        }
      }
      put("/sessions/:sessionId/files/rename") { req: SessionController.RenameRequest =>
        withSession(req.sessionId) { service =>
          service.rename(req.oldPath, req.newPath)
        }
      }

      post("/sessions/:sessionId/compile") { req: SessionController.CompileRequest =>
        withSession(req.sessionId) { service =>
          service.compile(req.path)
        }
      }
      post("/sessions/:sessionId/complete") { req: SessionController.CompleteRequest =>
        withSession(req.sessionId) { service =>
          service.complete(req.toMopeRequest)
        }
      }
      post("/sessions/:sessionId/simulate") { req: SessionController.FSimulateRequest =>
        withSession(req.sessionId) { service =>
          service.simulate(req.toMopeRequest).map { uri =>
            val location = req.request.uri.toString+s"?addr=${uri.toString}"
            response
              .ok(SessionController.SimulationResponse(new URI(location)))
              .location(location)
          }
        }
      }
      get("/sessions/:sessionId/simulate") { req: SessionController.FSimulateStatusRequest =>
        withSession(req.sessionId) { service =>
          service
            .simulationResults(req.addr)
            .map { results => //apply possible filter
              req.filter match {
                case Nil => results
                case lst =>
                  val set = lst.toSet
                  val filtered = results.variables.filter { case (name,_) => name=="time" || set(name) }
                  results.copy(variables = filtered)
              }
            }
            .flatMap {
              case SimulationResult(name, variables) if req.format=="csv" =>
                service.locateSimulationCsv(name)
                  .map {
                    case Some(file) => sendFile(response)("text/csv", file)
                    case None => response.notFound(s"no results for $name available!")
                  }
              case result@SimulationResult(name, _) if req.format == "chartjs" =>
                val originalVariablesSize = result.variables.values.head.size
                val variables = result.trimmedVariables(maxSimulationData.value)
                val headers = variables.keys.filterNot(k => k=="time").toList
                val tableData = (variables("time")+:headers.map(k => variables(k)).toSeq).transpose
                Future.value(SessionController.TableFormat(
                  name,
                  tableData,
                  "time"::headers,
                  if(originalVariablesSize>maxSimulationData.value) Some(s"The plot doesn't contain all variables ($originalVariablesSize). Variables stripped to ${variables.values.head.size}. ")
                  else None
                ))
              case results => Future.value(results)
            }
        }
      }
    }
}
