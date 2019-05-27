/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.util.Future
import webmodelica.models.{User, errors}
import webmodelica.services.{TokenGenerator, UserToken}
import webmodelica.stores.UserStore

package object controllers {
  def extractTokenProvider(tokenGenerator:TokenGenerator)(req:Request): Future[UserToken] = {
    val token = req.headerMap.getOrNull(constants.authorizationHeader)
    tokenGenerator.decode(token)
  }

  def extractUserProvider(userStore:UserStore, tokenGenerator:TokenGenerator)(req:Request): Future[User] = {
    for {
      token <- extractTokenProvider(tokenGenerator)(req)
      user <- userStore.findBy(token.username).flatMap(errors.notFoundExc("web-token contains invalid user informations!"))
    } yield user
  }

  def sendFile(builder:ResponseBuilder)(contentType:String, file:java.io.File): Response = {
    builder.ok
      .header("Content-Disposition", s"""attachment; filename="${file.getName}"""")
      .contentType(contentType)
      .file(file)
  }
}
