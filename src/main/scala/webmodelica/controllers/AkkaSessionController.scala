package webmodelica.controllers

import com.twitter.util.{Future => TFuture}
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
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
      def service(): TFuture[SessionService] = sessionRegistry.get(id).flatMap(errors.notFoundExc(s"Can't find a session for: $id"))
        (delete & pathEnd) {
        logger.debug(s"delete session $id")
        val future = sessionRegistry.killSession(id).map(_ => StatusCodes.NoContent).asScala
        complete(future)
      } ~
      pathPrefix("files") {
        (path("update") & post & entity(as[ModelicaFile])) { case file =>
          logger.debug(s"update file $file")
          val future = service().flatMap(_.update(file)).asScala
          complete(future)
        } ~
          (path("rename") & put & entity(as[AkkaSessionController.RenameRequest])) { req =>
          logger.debug(s"rename file $req")
          val future = service().flatMap(_.rename(req.oldPath, req.newPath)).asScala
          complete(future)
        } ~
          (path("upload") & storeUploadedFile("archive", tempDestination)) { case (metadata, file) =>
            val future = service().flatMap(_.extractArchive(file.toPath)).asScala
            complete(future)
        } ~
        (path(Segment) & delete) { pathStr =>
          val path = Paths.get(pathStr)
          logger.debug(s"delete file $path")
          val future = service().flatMap(_.delete(path)).map(_ => StatusCodes.NoContent).asScala
          complete(future)
        }
      } ~
      mopeRoutes(() => service())
    }

  }

  private def mopeRoutes(service: () => TFuture[SessionService]): Route = {
    import webmodelica.models.mope._
    import webmodelica.models.mope.requests._
    import webmodelica.models.mope.responses._
    (path("compile") & post & entity(as[FilePath])) { filePath =>
      val future = service().flatMap(_.compile(filePath.toPath)).asScala
      complete(future)
    } ~
      }
    }
  }
}
