/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.controllers

import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import io.circe.generic.JsonCodec
import webmodelica.stores._
import webmodelica.services._
import webmodelica.models._
import webmodelica.models.config.MopeClientConfig
import webmodelica.conversions.futures._
import java.nio.file.Paths
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
    with AkkaController {

  val fileStore: Project => FileStore = FileStore.fromProject(mopeConf.data.hostDirectory, _)
  val projectFiles: Project => Future[List[ModelicaPath]] = fileStore(_).files.asScala
  val projectFileTree: Project => Future[FileTree] = (p:Project) => fileStore(p).fileTree(Some(p.name)).asScala

  override val routes:Route = {
    (extractUser & pathPrefix("projects")) { (user:User) =>
      (get & pathEnd) { //secured route: GET /projects
        logger.debug(s"searching projects for $user")
        val projects = projectStore.byUsername(user.username).map(_.map(JSProject.apply)).asScala
        complete(projects)
      } ~
      (post & entity(as[AkkaProjectController.ProjectRequest]) & pathEnd) { case AkkaProjectController.ProjectRequest(owner, name) =>
        //secured route: POST /projects
          if(user.username == owner) {
            logger.debug(s"new project $name for $owner")
          val project = Project(owner,name)
          val jsProject = projectStore.add(project).map(_ => JSProject(project)).asScala
          complete(jsProject)
          } else {
          complete(errors.ResourceUsernameError("project"))
        }
      } ~
      pathPrefix(Segment) { (id:String) =>
        /** Searches for the project, that is referenced by the id contained in the route. */
        def projectFinder(): Future[Project] = extractProject(id, user.username)
        (get & pathEnd) { //secured route: GET /projects/:id
          logger.debug(s"lookup project $id")
          val project = projectFinder().map(JSProject.apply)
          complete(project)
        } ~
        (delete & pathEnd) { //secured route: DELETE /projects/:id
          logger.debug(s"deleting project $id")
          val noContent = projectStore.delete(id).map { _ => StatusCodes.NoContent }.asScala
          complete(noContent)
        } ~
        (path("copy") & post & entity(as[AkkaProjectController.CopyProjectRequest])) { copyReq =>
          //secured route: POST /projects/:id/copy
          logger.debug(s"copy project $copyReq")
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
            logger.debug(s"update visibility for $id:$visibility")
            complete(
              projectStore.setVisiblity(id, visibility)
                .map(JSProject.apply)
                .asScala
            )
        } ~
        fileRoutes(id, () => projectFinder())
      }
    }
  }

  private def fileRoutes(id:String, projectFinder: () => Future[Project]): Route = {
    pathPrefix("files") {
      (get & pathEnd & parameter("format" ? "list")) {
        //secured route: GET /projects/:id/files?format=[tree|list]
        case "tree" => complete(projectFinder().flatMap(projectFileTree))
        case _ => complete(projectFinder().flatMap(projectFiles))
      } ~
        (get & path("download")) {//secured route: GET /projects/:id/files/download
        logger.debug(s"download files for $id")
        val future = (for {
          project <- projectFinder()
          fs = fileStore(project)
          file <- fs.packageProjectArchive(project.name).asScala
        } yield file)
        onSuccess(future) { file =>
          respondWithHeader(RawHeader("Content-Disposition", s"attachment; filename=${file.getName}")) {
            getFromFile(file.getPath)
          }
        }
      } ~
        (get & path(Remaining)) { pathStr =>
        //secured route: GET /projects/:id/files/:path
        //(the uri is created to decode the uri-encoded path)
        logger.debug(s"fetch file content for $pathStr")
        val path = Paths.get(new java.net.URI(pathStr).getPath)
        logger.debug(s"searching for file $path")
        complete(
          projectFinder()
            .map(fileStore)
            .flatMap(_.findByPath(path).flatMap(errors.notFoundExc(s"file $path not found!")).asScala)
        )
      }
    }
  }

}
