package webmodelica.core

import com.twitter.inject.logging.MDCInitializer
import com.twitter.finatra.http.HttpServer
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter, StatsFilter}
import com.twitter.finatra.http.routing.HttpRouter
import webmodelica._

object WMServerMain extends WMServer

class WMServer extends HttpServer {

  MDCInitializer.init()

  override def jacksonModule = JsonConfigModule
  override val modules = Seq(AppModule)

  override def configureHttp(router: HttpRouter) {
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[StatsFilter[Request]]
      .filter[CommonFilters]
      .add[controllers.ProjectController]
      .add[controllers.SessionController]
      .add[controllers.InfoController]
      .add[controllers.UserController]
  }

  scala.sys.addShutdownHook { this.close() }
}
