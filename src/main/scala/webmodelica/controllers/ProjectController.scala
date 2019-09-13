/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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
import java.net.URI

import webmodelica.models._
import webmodelica.models.config.MopeClientConfig
import webmodelica.stores.FileStore
import webmodelica.conversions.futures._
import webmodelica.services.{TokenGenerator, TokenValidator, UserToken}
import webmodelica.stores.{ProjectStore, UserStore}

object ProjectController {
  case class ProjectRequest(
                             owner: String,
                             name: String,
                             request: com.twitter.finagle.http.Request)

  case class CopyProjectRequest(
                                 @RouteParam() id: String,
                                 @JsonProperty() name: Option[String],
                                 request: Request,
                               ) {
    def newProject(p: Project, owner: String): Project = {
      Project(
        this.into[ProjectRequest]
          .withFieldComputed(_.owner, _ => owner)
          .withFieldComputed(_.name, req => req.name.getOrElse(p.name))
          .withFieldComputed(_.request, req => req.request)
          .transform
      )
    }
  }

  case class FileContentRequest(
                                 @RouteParam() id: String,
                                 @RouteParam() path: URI,
                                 request: Request,
                               ) {
    def asPath: Path = Paths.get(path.getPath)
  }

  case class ProjectFilesRequest(
                                  @RouteParam() id: String,
                                  @QueryParam() format: String = "list",
                                  request: Request,
                                )

  case class VisibilityRequest(
                                @RouteParam() id: String,
                                @JsonProperty() visibility: String)

}
class ProjectController@Inject()(
    override val projectStore:ProjectStore,
    prefix:webmodelica.ApiPrefix,
    mopeConf:MopeClientConfig,
    override val userStore: UserStore,
    override val gen: TokenValidator)
    extends Controller
    with UserExtractor
    with ProjectExtractor {

  val fileStore: Project => FileStore = FileStore.fromProject(mopeConf.data.hostDirectory, _)
  val projectFiles: Project => Future[List[ModelicaPath]] = fileStore(_).files
  val projectFileTree: Project => Future[FileTree] = (p:Project) => fileStore(p).fileTree(Some(p.name))

  filter[JwtFilter]
    .prefix(prefix.p) {
      post("/projects") { project: ProjectController.ProjectRequest =>
        extractUsername(project.request).flatMap {
          case username if username == project.owner =>
            val newProj = Project(project)
            projectStore.add(newProj).map(_ => JSProject(newProj))
          case _ => Future.exception(errors.ResourceUsernameError("project"))
        }
      }

      get("/projects/:id") { requ: Request =>
        val id = requ.getParam("id")
          extractUsername(requ).flatMap { username =>
            extractProject(id, username).map(JSProject.apply)
          }
      }

      delete("/projects/:id") { requ:Request =>
        projectStore.delete(requ.getParam("id"))
          .map(_ => response.noContent)
      }

      post("/projects/:id/copy") { copyReq: ProjectController.CopyProjectRequest =>
        (for {
          username <- extractUsername(copyReq.request)
          project <- extractProject(copyReq.id, username)
          newProject = copyReq.newProject(project, username)
          _ <- projectStore add newProject
          _ <- fileStore(project) copyTo fileStore(newProject).rootDir
        } yield JSProject(newProject))
      }

      put("/projects/:id/visibility") { visibilityReq: ProjectController.VisibilityRequest =>
        projectStore.setVisiblity(visibilityReq.id, visibilityReq.visibility)
          .map(JSProject.apply)
          .handle {
            case e:IllegalArgumentException =>
              response.badRequest(e.getMessage)
          }
      }

      get("/projects/:id/files") { requ: ProjectController.ProjectFilesRequest =>
        for {
          username <- extractUsername(requ.request)
          project <- extractProject(requ.id, username)
          files <- if(requ.format.toLowerCase == "tree") projectFileTree(project) else  projectFiles(project)
        } yield files
      }
      get("/projects/:id/files/download") { requ: Request =>
        val id = requ.getParam("id")
        for {
          username <- extractUsername(requ)
          project <- extractProject(id, username)
          file <- fileStore(project).packageProjectArchive(project.name)
        } yield sendFile(response)("application/zip", file)
      }
      get("/projects/:id/files/:path") { req: ProjectController.FileContentRequest =>
        for {
          username <- extractUsername(req.request)
          project <- extractProject(req.id, username)
          file <- fileStore(project).findByPath(req.asPath).flatMap(errors.notFoundExc(s"file ${req.asPath} not found!"))
        } yield file
      }

      get("/projects") { requ: Request =>
        extractUsername(requ)
          .flatMap(projectStore.byUsername)
          .map(_.map(JSProject.apply))
      }
   }
}
