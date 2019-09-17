package webmodelica.core

import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives.{extractRequest, mapResponse}

object AccessLog {
  private val accessLog = com.typesafe.scalalogging.Logger("AccessLog")
  def logRequestResponse: Directive0 = extractRequest.flatMap { request =>
    mapResponse { response =>
      val prefixStr = s"""[${request.method.value}] ${request.uri} -> ${response.status} : ${response.entity}"""
      if(response.status.isFailure()) {
        accessLog.warn(prefixStr)
      } else {
        accessLog.info(prefixStr)
      }
      response
    }
  }
}
