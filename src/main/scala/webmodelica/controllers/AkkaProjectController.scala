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


  logger.debug(s"validator is $gen")

  val routes:Route = (extractUser & path("projects") & pathEnd & get) { user =>
    logger.debug(s"searching projects for $user")
    val projects = projectStore.byUsername(user.username).map(_.map(JSProject.apply)).asScala
    complete(projects)
  }
}
