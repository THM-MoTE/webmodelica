package webmodelica.controllers

import com.twitter.util.{Future => TFuture}
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Location
import webmodelica.models._
import webmodelica.models.config._
import webmodelica.services._
import webmodelica.stores._
import webmodelica.conversions.futures._
import io.scalaland.chimney.dsl._
import java.nio.file.{
  Paths,
  Path
}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import io.circe.generic.JsonCodec

object AkkaSessionController {
  @JsonCodec
  case class RenameRequest(
    oldPath: Path,
    newPath: Path,
  )
  @JsonCodec
  case class SimulationResponse(location: String)
}

class AkkaSessionController(
  override val userStore: UserStore,
  override val projectStore:ProjectStore,
  override val gen: CombinedTokenValidator,
  sessionRegistry: SessionRegistry,
  maxSimulationData:MaxSimulationData)
    extends AkkaUserExtractor
    with AkkaProjectExtractor
    with com.typesafe.scalalogging.LazyLogging
    with de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
    with AkkaController {
  override val routes:Route = (logRequest("/sessions") & extractUser) { (user:User) =>
    (path("projects" / Segment / "sessions" / "new") & post) { projectId =>
      //secured route: POST /projects/:id/sessions/new
      logger.debug(s"new session for $projectId")
      complete(for {
          project <- extractProject(projectId, user.username)
          (service, session) <- sessionRegistry.create(project).asScala
          files <- service.files.asScala
        } yield {
          //only connect if not already done: we did if we have an id
          if(session.mopeId.isEmpty) {
            service.connect()
          }
          JSSession(session, files)
        })
    } ~
    pathPrefix("sessions" / Segment) { (id:String) =>
      //secured route: /sessions/:id
      def service(): TFuture[SessionService] = sessionRegistry.get(id).flatMap(errors.notFoundExc(s"Can't find a session for: $id"))
        (delete & pathEnd) { //secured route: DELETE /sessions/:id
        logger.debug(s"delete session $id")
        val future = sessionRegistry.killSession(id).map(_ => StatusCodes.NoContent).asScala
        complete(future)
      } ~
      pathPrefix("files") { //secured route: /sessions/:id/files
        (path("update") & post & entity(as[ModelicaFile])) { case file =>
          //secured route: POST /sessions/:id/files/update
          logger.debug(s"update file $file")
          val future = service().flatMap(_.update(file)).asScala
          complete(future)
        } ~
          (path("rename") & put & entity(as[AkkaSessionController.RenameRequest])) { req =>
          //secured route: PUT /sessions/:id/files/rename
          logger.debug(s"rename file $req")
          val future = service().flatMap(_.rename(req.oldPath, req.newPath)).asScala
          complete(future)
        } ~
          (path("upload") & storeUploadedFile("archive", tempDestination)) { case (metadata, file) =>
            //secured route: POST /sessions/:id/files/upload
            val future = service().flatMap(_.extractArchive(file.toPath)).asScala
            complete(future)
        } ~
          (path(Segment) & delete) { pathStr =>
          //secured route: DELETE /sessions/:id/files/:path
          val path = Paths.get(pathStr)
          logger.debug(s"delete file $path")
          val future = service().flatMap(_.delete(path)).map(_ => StatusCodes.NoContent).asScala
          complete(future)
        }
      } ~
      mopeRoutes(() => service()) ~
      simulateRoutes(() => service())
    }

  }

  import webmodelica.models.mope._
  import webmodelica.models.mope.requests._
  import webmodelica.models.mope.responses._

  private def mopeRoutes(service: () => TFuture[SessionService]): Route = {
    (path("compile") & post & entity(as[FilePath])) { filePath =>
      //secured route: POST /sessions/:id/compile
      val future = service().flatMap(_.compile(filePath.toPath)).asScala
      complete(future)
    } ~
      (path("complete") & post & entity(as[Complete])) { completeReq =>
      //secured route: POST /sessions/:id/compile
      val future = service().flatMap(_.complete(completeReq)).asScala
      complete(future)
    }
  }

  private def simulateRoutes(service: () => TFuture[SessionService]): Route = {
    (path("simulate") & extractUri) { uri =>
      //secured route: /sessions/:id/simulate
      (post & pathEnd & entity(as[SimulateRequest])) { simReq =>
        //secured route: POST /sessions/:id/simulate
          val future = service().flatMap(_.simulate(simReq)).asScala
          onSuccess(future) { mopeUri =>
            val location = uri.withQuery(Uri.Query("addr" -> mopeUri.toString))
            respondWithHeader(headers.Location(location)) {
              complete(StatusCodes.Accepted -> AkkaSessionController.SimulationResponse(location.toString))
            }
          }
        } ~
        (get & parameter("format" ? "default") & parameter("addr")) { case (format, addrStr) =>
          //secured route: GET /sessions/:id/simulate?format=[default, chartjs]&addr=[mope-uri]&filter=[variable-filter]
            val addr = new java.net.URI(addrStr)
            parameter("filter"?) { filterStrOpt =>
              //first apply filter & fetch the results
              val filter = filterStrOpt.map { str => str.split(',').map(_.trim).toList }.getOrElse(List.empty[String])
              val resultFuture = service()
                .flatMap(_.simulationResults(addr))
                .map(_.filterVariables(filter))
              //then transform results according to the filter
              format match {
                case "csv" =>
                  //just return the underlying file,
                  //the file must be located first based on the model's name
                  val fileFuture = for {
                    result <- resultFuture
                    svc <- service()
                    csvOpt <- svc.locateSimulationCsv(result.modelName)
                  } yield (result, csvOpt)
                  onSuccess(fileFuture.asScala) {
                    case (_, Some(file)) => getFromFile(file.getPath)
                    case (SimulationResult(name, _), None) => complete(StatusCodes.NotFound -> s"no results for $name available!")
                  }
                case "chartjs" => complete(resultFuture.map(r => TableFormat(maxSimulationData, r)).asScala)
                case _ => complete(resultFuture.asScala)
              }
            }
        }
    }
  }
}
