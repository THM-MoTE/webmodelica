package webmodelica.core

import com.twitter.finatra.http.HttpServer
import com.twitter.finagle.http.{Request, Response}

import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import webmodelica._

object WMServerMain extends WMServer

class WMServer extends HttpServer {

  override def jacksonModule = JsonConfigModule
  override val modules = Seq(AppModule)

  override def configureHttp(router: HttpRouter) {
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[CommonFilters]
      .add[controllers.Simple]
      .add[controllers.ProjectController]
      .add[controllers.SessionController]
  }
}
