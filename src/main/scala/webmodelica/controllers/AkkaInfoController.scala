package webmodelica.controllers

import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import webmodelica.models.Infos

object AkkaInfoController
    extends com.typesafe.scalalogging.LazyLogging
    with de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
    with AkkaController {
  val info = Infos()

  override val routes:Route = (path("info") & get & pathEnd) {
    complete(info)
  }
}
