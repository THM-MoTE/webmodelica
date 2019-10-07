/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.services

import com.twitter.util.Future
import scala.concurrent.{Future => SFuture}
import akka.actor.ActorSystem
import akka.http.scaladsl._
import akka.http.scaladsl.model._
import webmodelica.models.{AuthUser, User}
import webmodelica.models.config.UserServiceConf
import webmodelica.stores.UserStore
import webmodelica.conversions.futures._
import webmodelica.models.errors.{
  UserServiceError,
  UserAlreadyInUse
}
import io.circe.generic.JsonCodec

@JsonCodec
private[services] case class UserWrapper(data:AuthUser)

/** A UserService that talks to the UserSvc. */
class UserServiceProxy(conf:UserServiceConf)(implicit system:ActorSystem)
  extends UserStore
    with com.typesafe.scalalogging.LazyLogging {
  import io.circe.Json
  import io.circe.generic.auto._
  import io.circe.syntax._

  val headers = Seq(
    "Service" -> "auth"
  )
  import system.dispatcher
  val client = new AkkaHttpClient(Http(), Uri(conf.address+ "/"+ conf.resource+"/"))
  logger.info("UserServiceProxy started.")

  override def add(u: User): Future[Unit] = {
    logger.debug(s"adding $u")
    client.postJson[Json, UserWrapper]("", Json.obj("user" -> u.asJson), headers)
      .recoverWith {
        case AkkaHttpClient.Error(StatusCodes.Conflict, _, _) => SFuture.failed(UserAlreadyInUse)
      }
      .asTwitter
      .unit
  }

  override def findBy(username: String): Future[Option[User]] = {
    logger.debug(s"searching $username")
    client.getJson[UserWrapper](username, headers)
      .map { wrapper =>
        logger.debug(s"searching $username returned ${wrapper.data}")
        Some(wrapper.data.toUser)
      }
      .recoverWith {
        case AkkaHttpClient.Error(StatusCodes.NotFound, _, _) =>
          logger.debug(s"searching $username returned NotFound")
          SFuture.successful(None)
      }
      .asTwitter
  }
}
