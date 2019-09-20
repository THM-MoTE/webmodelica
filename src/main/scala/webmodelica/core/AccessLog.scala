/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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
