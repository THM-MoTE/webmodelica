/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.controllers

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import webmodelica.services._
import webmodelica.stores._
import webmodelica.models.{Project, User, errors}
import webmodelica.conversions.futures._
import com.typesafe.scalalogging.LazyLogging
import webmodelica.constants

trait AkkaJwtExtractor {
  this: LazyLogging =>

  def jwt:Directive1[String] = optionalHeaderValue {
    case auth:Authorization =>
      auth.value match {
        case AkkaJwtExtractor.bearerExtractor(token) => Some(token)
        case _ => None
      }
    case cookie:Cookie =>
      cookie.cookies.find(c => c.name.toLowerCase == "token").map(_.value)
    case _ => None
  }.flatMap {
    case Some(s) => provide(s)
    case None => complete(StatusCodes.Unauthorized ->
                    s"""Authorization missing!
                    |Either provide:
                    |- the cookie token=<jwt-token>
                    |- ${constants.authorizationHeader}: Bearer <jwt-token>""".stripMargin)
  }
}
object AkkaJwtExtractor {
  //strips 'Bearer' from the authorization header (see: https://de.wikipedia.org/wiki/JSON_Web_Token#Header).
  val bearerExtractor = """^Bearer\s+([\w\-.]+)$""".r
}

trait AkkaUserExtractor
    extends AkkaJwtExtractor {
  this: LazyLogging =>
  def userStore: UserStore
  def gen: TokenValidator
  def extractUser:Directive1[User] = jwt.flatMap { rawJwt =>
    lazy val fetchFromStore = for {
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

trait AkkaProjectExtractor {
  def projectStore: ProjectStore

  def extractProject(id:String, username:String): Future[Project] =
    projectStore.findBy(id, username).flatMap(errors.notFoundExc(s"project with $id not found!")).asScala
}
