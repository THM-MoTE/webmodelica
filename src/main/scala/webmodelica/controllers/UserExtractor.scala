/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.controllers

import com.twitter.finatra.http.Controller
import com.twitter.finagle.http.Request
import com.twitter.util.Future
import webmodelica.stores.UserStore
import webmodelica.services.{TokenValidator, UserToken}
import webmodelica.models.{User, errors}

trait UserExtractor {
  this: com.twitter.inject.Logging =>
  def userStore: UserStore
  def gen: TokenValidator

  def extractToken(req:Request): Future[UserToken] =
    errors.notFoundExc("no web-token provided!")(req.authorization).flatMap(gen.decode)

  def extractUser(req:Request): Future[User] = {
    val fetchFromStore = for {
      token <- extractToken(req)
      userOpt <- userStore.findBy(token.username)
      user <- errors.notFoundExc("web-token contains invalid user informations!")(userOpt)
    } yield user

    val fetchFromToken = errors.notFoundExc("no web-token provided!")(req.authorization).flatMap(gen.decodeToUser)
    fetchFromToken.flatMap {
      case Some(user) =>
        debug(s"got user informatoins from token: $user")
        Future.value(user)
      case None =>
        warn(s"didn't find user informations in token; try using the UserStore")
        fetchFromStore
    }
  }

  def extractUsername(req:Request): Future[String] =
    extractToken(req).map(_.username)
}
