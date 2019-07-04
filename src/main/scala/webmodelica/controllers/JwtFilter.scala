/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.controllers

import com.google.inject.Inject
import com.twitter.finagle.{Service, SimpleFilter, http}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.util.Future
import webmodelica.constants
import webmodelica.services.{TokenGenerator, TokenValidator}
import webmodelica.stores.UserStore
import webmodelica.models.{User, errors}


class JwtFilter@Inject()(gen:TokenGenerator, store:UserStore)(validator:TokenValidator=gen) extends SimpleFilter[http.Request, http.Response]
  with com.twitter.inject.Logging {

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    val headerField = request.headerMap.get(constants.authorizationHeader)
    lazy val cookie = request.cookies.get("token").map(_.value)
    val resultOpt = headerField.orElse(cookie).filter(validator.isValid).map { token =>
      //explictly set the Authorization header because it could be inside of a cookie
      //which isn't used when extracting user informations
      request.authorization = token
      service(request)
    }
    resultOpt.getOrElse {
        warn(s"provided token invalid!")
        val res = Response()
        res.status = Status.Unauthorized
        res.contentString = "Invalid web-token!"
        Future.value(res)
    }
  }
}
