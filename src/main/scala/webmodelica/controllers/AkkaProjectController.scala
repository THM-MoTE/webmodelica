package webmodelica.controllers

import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import com.fasterxml.jackson.annotation.JsonProperty
import com.twitter.finagle.http.Request
import com.twitter.finatra.request.RouteParam
import io.circe.generic.JsonCodec
import webmodelica.controllers.ProjectController.ProjectRequest
import webmodelica.stores._
import webmodelica.services._
import webmodelica.models._
import webmodelica.models.config.MopeClientConfig
import webmodelica.conversions.futures._
import io.scalaland.chimney.dsl._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AkkaProjectController {
  @JsonCodec
  case class ProjectRequest(owner: String,
                            name: String)

  @JsonCodec
  case class CopyProjectRequest(name: Option[String]) {
    def newProject(p: Project, owner: String): Project = Project(owner, name.getOrElse(p.name))
  }

  @JsonCodec
  case class VisibilityRequest(visibility:String) extends AnyVal
}

class AkkaProjectController(
  override val projectStore:ProjectStore,
  mopeConf:MopeClientConfig,
  override val userStore: UserStore,
  override val gen: CombinedTokenValidator)
    extends AkkaUserExtractor
    with AkkaProjectExtractor
    with com.typesafe.scalalogging.LazyLogging
    with de.heikoseeberger.akkahttpcirce.FailFastCirceSupport {

  val fileStore: Project => FileStore = FileStore.fromProject(mopeConf.data.hostDirectory, _)
  val projectFiles: Project => Future[List[ModelicaPath]] = fileStore(_).files.asScala
  val projectFileTree: Project => Future[FileTree] = (p:Project) => fileStore(p).fileTree(Some(p.name)).asScala

  //TODO: map options to 404 errors; currently result is 200 with empty body

  val routes:Route = logRequest("/projects") {
    (extractUser & pathPrefix("projects")) { (user:User) =>
      (get & pathEnd) { //secured route: GET /projects
        logger.debug(s"searching projects for $user")
        val projects = projectStore.byUsername(user.username).map(_.map(JSProject.apply)).asScala
        complete(projects)
      } ~
      (post & entity(as[AkkaProjectController.ProjectRequest]) & pathEnd) { case AkkaProjectController.ProjectRequest(owner, name) =>
        //secured route: POST /projects
        if(user.username == owner) {
          val project = Project(owner,name)
          val jsProject = projectStore.add(project).map(_ => JSProject(project)).asScala
          complete(jsProject)
        } else {
          complete(errors.ResourceUsernameError("project"))
        }
      } ~
      pathPrefix(Segment) { (id:String) =>
        def projectFinder(): Future[Project] = extractProject(id, user.username)
        get { //secured route: GET /projects/:id
        logger.debug(s"lookup project $id")
        val project = projectStore.findBy(id, user.username).map(_.map(JSProject.apply)).asScala
          complete(project)
        } ~
        delete { //secured route: DELETE /projects/:id
          logger.debug(s"deleting project $id")
          val noContent = projectStore.delete(id).map { _ => HttpResponse(StatusCodes.NoContent) }.asScala
          complete(noContent)
        } ~
        (path("copy") & post & entity(as[AkkaProjectController.CopyProjectRequest])) { copyReq =>
          //secured route: POST /projects/:id/copy
          complete(
            for {
              project <- projectFinder()
              newProject = copyReq.newProject(project, user.username)
              _ <- (projectStore add newProject).asScala
              _ <- (fileStore(project) copyTo fileStore(newProject).rootDir).asScala
            } yield JSProject(newProject)
          )
        } ~
          (path("visibility") & put & entity(as[AkkaProjectController.VisibilityRequest])) { case AkkaProjectController.VisibilityRequest(visibility) =>
            //secured route: PUT /projects/:id/visibility
            complete(
              projectStore.setVisiblity(id, visibility)
                .map(JSProject.apply)
                .asScala
            )
        }
      }
    }
  }}
