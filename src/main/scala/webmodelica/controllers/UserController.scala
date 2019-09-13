/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.controllers

import java.security.MessageDigest

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.inject.Inject
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.util.Future
import webmodelica.services.{TokenGenerator, UserToken}
import webmodelica.stores.{ProjectStore, UserStore}
import io.scalaland.chimney.dsl._
import webmodelica.constants
import webmodelica.models.{User, errors}

object UserController {
  case class RegisterRequest(
                              @JsonProperty() username: String,
                              @JsonProperty() email: String,
                              @JsonProperty() password: String) {
    def toSecureUser: MessageDigest => User =
      UserStore.securePassword(_)(
        this.into[User]
          .withFieldRenamed(_.password, _.hashedPassword)
          .withFieldComputed(_.username, _.username.toLowerCase)
          .withFieldComputed(_.first_name, _ => None)
          .withFieldComputed(_.last_name, _ => None)
          .transform)
  }

  case class LoginRequest(@JsonProperty() username: String,
                          @JsonProperty() password: String)

  case class TokenResponse(token: String)

}
class UserController@Inject()(userStore:UserStore,
                              tokenGenerator:TokenGenerator,
                              digest: MessageDigest,
                              prefix:webmodelica.ApiPrefix)
  extends Controller {

  private def tokenResponse(token:String) = response.ok(UserController.TokenResponse(token)).header(constants.authorizationHeader, token)

  prefix(prefix.p) {
    post("/users/register") { req: UserController.RegisterRequest =>
      val user = req.toSecureUser(digest)
      debug(s"registering: $user")
      userStore.add(user)
        .map(_ => tokenGenerator.newToken(user))
        .map(tokenResponse)
    }

    post("/users/login") { req: UserController.LoginRequest =>
      userStore.findBy(req.username.toLowerCase)
        .flatMap {
          case Some(dbUser) if dbUser.hashedPassword == UserStore.hashString(digest)(req.password) => Future.value(dbUser)
          case _ => Future.exception(errors.CredentialsError)
        }
        .map(u => tokenGenerator.newToken(u))
        .map(tokenResponse)
    }

    filter[JwtFilter]
      .post("/users/refresh") { req: Request =>
        val token = req.headerMap.getOrNull(constants.authorizationHeader)
        for {
          token <- tokenGenerator.decode(token)
          user <- userStore.findBy(token.username).flatMap(errors.notFoundExc("web-token contains invalid user informations!"))
        } yield tokenResponse(tokenGenerator.newToken(user))
      }
  }
}
