package webmodelica.controllers

import java.security.MessageDigest

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.inject.Inject
import com.twitter.finatra.http.Controller
import com.twitter.util.Future
import webmodelica.services.TokenGenerator
import webmodelica.stores.{ProjectStore, UserStore}
import io.scalaland.chimney.dsl._
import webmodelica.models.{User, errors}

case class RegisterRequest(
  @JsonProperty() username:String,
  @JsonProperty() email:String,
  @JsonProperty() password:String) {
  def toSecureUser: MessageDigest => User =
    UserStore.securePassword(_)(this.into[User].withFieldRenamed(_.password, _.hashedPassword).transform)
}

case class LoginRequest(@JsonProperty() username:String,
                       @JsonProperty() password:String)

case class TokenResponse(token:String)

class UserController@Inject()(userStore:UserStore,
                              tokenGenerator:TokenGenerator,
                              digest: MessageDigest)
  extends Controller {

  private def tokenResponse(token:String) = response.ok(TokenResponse(token)).header("Authorization", token)

  post("/users/register") { req:RegisterRequest =>
    val user = req.toSecureUser(digest)
    debug(s"registering: $user")
    userStore.add(user)
      .map(_ => tokenGenerator.newToken(user))
      .map(tokenResponse)
  }

  post("/users/login") { req: LoginRequest =>
    //TODO handle filter errors !
    userStore.findBy(req.username)
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
}
