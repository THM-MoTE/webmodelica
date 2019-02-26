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

case class RegisterRequest(
  @JsonProperty() username:String,
  @JsonProperty() email:String,
  @JsonProperty() password:String) {
  def toSecureUser: MessageDigest => User =
    UserStore.securePassword(_)(
      this.into[User]
        .withFieldRenamed(_.password, _.hashedPassword)
        .withFieldComputed(_.username, _.username.toLowerCase)
        .transform)
}

case class LoginRequest(@JsonProperty() username:String,
                       @JsonProperty() password:String)

case class TokenResponse(token:String)

class UserController@Inject()(userStore:UserStore,
                              tokenGenerator:TokenGenerator,
                              digest: MessageDigest,
                              prefix:webmodelica.ApiPrefix)
  extends Controller {

  private def tokenResponse(token:String) = response.ok(TokenResponse(token)).header(constants.authorizationHeader, token)

  prefix(prefix.p) {
    post("/users/register") { req: RegisterRequest =>
      val user = req.toSecureUser(digest)
      debug(s"registering: $user")
      userStore.add(user)
        .map(_ => tokenGenerator.newToken(user))
        .map(tokenResponse)
        .handle {
          case e:errors.UsernameAlreadyInUse => response.conflict(e.getMessage)
        }
    }

    post("/users/login") { req: LoginRequest =>
      userStore.findBy(req.username.toLowerCase)
        .flatMap {
          case Some(dbUser) if dbUser.hashedPassword == UserStore.hashString(digest)(req.password) => Future.value(dbUser)
          case _ => Future.exception(errors.CredentialsError)
        }
        .map(u => tokenGenerator.newToken(u))
        .map(tokenResponse)
        .handle {
          case errors.CredentialsError => response.unauthorized(errors.CredentialsError.getMessage)
        }
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
