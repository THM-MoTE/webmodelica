package webmodelica.controllers

import com.google.inject.Inject
import com.twitter.util.Future
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.fasterxml.jackson.annotation.JsonProperty
import com.twitter.finatra.request._
import cats.implicits._
import io.scalaland.chimney.dsl._
import org.mongodb.scala.bson.BsonObjectId
import java.nio.file.{Path, Paths}
import webmodelica.models._
import webmodelica.models.config.MopeClientConfig
import webmodelica.stores.FileStore
import webmodelica.conversions.futures._
import webmodelica.services.{
  TokenGenerator,
  UserToken
}
import webmodelica.stores.{
  ProjectStore,
  UserStore
}

case class CopyProjectRequest(
  @RouteParam() projectId: String,
  @JsonProperty() name: Option[String],
  request: Request,
) {
  def newProject(p:Project, owner:String): Project = {
    Project(
      this.into[ProjectRequest]
        .withFieldComputed(_.owner, _ => owner)
        .withFieldComputed(_.name, req => req.name.getOrElse(p.name))
        .withFieldComputed(_.request, req => req.request)
        .transform
    )
  }
}

case class VisibilityRequest(
  @RouteParam() projectId: String,
  @JsonProperty() visibility: String)

class ProjectController@Inject()(
  store:ProjectStore,
  prefix:webmodelica.ApiPrefix,
  mopeConf:MopeClientConfig,
  override val userStore: UserStore,
  override val gen: TokenGenerator)
    extends Controller
    with UserExtractor {

  val fileStore: Project => FileStore = FileStore.fromProject(mopeConf.data.hostDirectory, _)
  val projectFiles: Project => Future[List[ModelicaFile]] = fileStore(_).files

  filter[JwtFilter]
    .prefix(prefix.p) {
      post("/projects") { project: ProjectRequest =>
        extractToken(project.request).flatMap {
          case token if token.username == project.owner =>
            val newProj = Project(project)
            store.add(newProj).map(_ => JSProject(newProj))
          case _ => Future.value(response.forbidden.body(errors.ResourceUsernameError("project").getMessage))
        }.handle {
          case e:errors.AlreadyInUse => response.conflict(e.getMessage)
        }
      }

      get("/projects/:id") { requ: Request =>
        val id = requ.getParam("id")
          extractToken(requ).flatMap { case UserToken(username,_,_) =>
            store.findBy(id, username)
              .flatMap(errors.notFoundExc(s"project with $id not found!"))
              .map(JSProject.apply)
          }
      }

      post("/projects/:projectId/copy") { copyReq: CopyProjectRequest =>
        val id = copyReq.projectId
        (for {
          UserToken(username,_,_) <- extractToken(copyReq.request)
          project <- store.findBy(id, username).flatMap(errors.notFoundExc(s"project with $id not found!"))
          newProject = copyReq.newProject(project, username)
          _ <- store add newProject
          _ <- fileStore(project) copyTo fileStore(newProject).rootDir
        } yield JSProject(newProject))
          .handle {
            case e:errors.AlreadyInUse => response.conflict(e.getMessage)
          }
      }

      put("/projects/:projectId/visibility") { visibilityReq: VisibilityRequest =>
        store.setVisiblity(visibilityReq.projectId, visibilityReq.visibility)
          .handle {
            case e:IllegalArgumentException =>
              response.badRequest(e.getMessage)
          }
      }

      get("/projects/:id/files") { requ: Request =>
        val id = requ.getParam("id")
        for {
          UserToken(username,_,_) <- extractToken(requ)
          project <- store.findBy(id, username).flatMap(errors.notFoundExc(s"project with $id not found!"))
          files <- projectFiles(project)
        } yield files
      }
      get("/projects/:id/files/download") { requ: Request =>
        val id = requ.getParam("id")
        for {
          UserToken(username,_,_) <- extractToken(requ)
          project <- store.findBy(id, username).flatMap(errors.notFoundExc(s"project with $id not found!"))
          file <- fileStore(project).packageProjectArchive(project.name)
        } yield sendFile(response)("application/zip", file)
      }

      get("/projects") { requ: Request =>
        extractToken(requ).flatMap { case UserToken(username,_,_) =>
          store.byUsername(username).map(_.map(JSProject.apply))
        }
      }
   }
}
