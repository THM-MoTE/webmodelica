package webmodelica.controllers

import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import webmodelica.stores._
import webmodelica.services._
import webmodelica.models._
import webmodelica.models.config.MopeClientConfig
import webmodelica.conversions.futures._
import scala.concurrent.ExecutionContext.Implicits.global

class AkkaProjectController(
  val projectStore:ProjectStore,
  mopeConf:MopeClientConfig,
  override val userStore: UserStore,
  override val gen: CombinedTokenValidator)
    extends AkkaUserExtractor
    with com.typesafe.scalalogging.LazyLogging
    with de.heikoseeberger.akkahttpcirce.FailFastCirceSupport {

  //TODO: map options to 404 errors; currently result is 200 with empty body

  val routes:Route = logRequest("/projects") {
    (extractUser & pathPrefix("projects")) { (user:User) =>
      (get & pathEnd) { //secured route: /projects
        logger.debug(s"searching projects for $user")
        val projects = projectStore.byUsername(user.username).map(_.map(JSProject.apply)).asScala
        complete(projects)
      } ~
        (get & path(Segment)) { (id:String) => //secured route: /projects/:id
        logger.debug(s"lookup project $id")
        val project = projectStore.findBy(id, user.username).map(_.map(JSProject.apply)).asScala
        complete(project)
      }
    }
  }}
