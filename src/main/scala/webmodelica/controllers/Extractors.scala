package webmodelica.controllers

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import webmodelica.services._
import webmodelica.stores._
import webmodelica.models.errors
import webmodelica.models.User
import webmodelica.conversions.futures._
import com.typesafe.scalalogging.LazyLogging

trait AkkaJwtExtractor {
  this: LazyLogging =>
  def jwt:Directive1[String] = headerValue {
    case auth:Authorization => Some(auth.value)
    case cookie:Cookie =>
      cookie.cookies.find(c => c.name.toLowerCase == "token").map(_.value)
    case _ => None
  }
}

trait AkkaUserExtractor
    extends AkkaJwtExtractor {
  this: LazyLogging =>
  def userStore: UserStore
  def gen: TokenValidator
  def extractUser:Directive1[User] = jwt.flatMap { rawJwt =>
    logger.debug(s"jwt is $rawJwt")
    val fetchFromStore = for {
      token <- gen.decode(rawJwt)
      userOpt <- userStore.findBy(token.username)
      user <- errors.notFoundExc("web-token contains invalid user informations!")(userOpt)
    } yield user
    val fetchFromToken = gen.decodeToUser(rawJwt)
    val future = fetchFromToken.asScala.flatMap {
      case Some(user) =>
        logger.debug(s"got user informations from token: $user")
        Future.successful(user)
      case None =>
        logger.warn(s"didn't find user informations in token; try using the UserStore")
        fetchFromStore.asScala
    }
    onSuccess(future)
  }
}
