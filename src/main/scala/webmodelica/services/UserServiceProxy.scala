/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.services

import com.twitter.finagle.http.Status
import com.twitter.util.Future
import webmodelica.models.{AuthUser, User}
import webmodelica.models.config.UserServiceConf
import webmodelica.stores.UserStore
import featherbed._
import webmodelica.models.errors.{
  UserServiceError,
  UserAlreadyInUse
}
import io.circe.generic.JsonCodec

@JsonCodec
private[services] case class UserWrapper(data:AuthUser)

/** A UserService that talks to the UserSvc. */
class UserServiceProxy(conf:UserServiceConf)
  extends UserStore
    with com.twitter.inject.Logging {
  import featherbed.circe._
  import io.circe.Json
  import io.circe.generic.auto._
  import io.circe.syntax._

  val url = new java.net.URL(conf.address+ "/"+ conf.resource+"/")
  val headers = Seq(
    "Service" -> "auth"
  )
  def clientProvider() = new featherbed.Client(url)

  info("UserServiceProxy started.")
  info(s"user-service at $url")

  private def withClient[A](fn: featherbed.Client => Future[A]): Future[A] = {
    debug("Using new client")
    val cl = clientProvider()
    fn(cl).ensure {
      debug("releasing client")
      cl.close()
    }
  }

  override def add(u: User): Future[Unit] = {
    debug(s"adding $u")
    withClient { client =>
      val req = client.post("")
        .withHeaders(headers:_*)
        .withContent(Json.obj("user" -> u.asJson), "application/json")
        .accept("application/json")

      req.send[UserWrapper]().unit
        .handle {
          case request.ErrorResponse(req, resp) if resp.status == Status.Conflict => throw UserAlreadyInUse
          case request.ErrorResponse(req, resp) =>
            val str = s"Error in 'add' $resp to request $req"
            throw UserServiceError(str)
        }
    }
  }

  override def findBy(username: String): Future[Option[User]] = {
    debug(s"searching $username")
    withClient { client =>
      val req = client.get(s"$username")
        .withHeaders(headers:_*)
        .accept("application/json")

      req.send[UserWrapper]().map { wrapper =>
        debug(s"searching $username returned ${wrapper.data}")
        Some(wrapper.data.toUser)
      }
        .handle {
          case request.ErrorResponse(req,resp) if resp.status == Status.NotFound =>
            debug(s"searching $username returned NotFound")
            None
          case request.ErrorResponse(req,resp) =>
            val str = s"Error in 'findBy' $resp to request $req"
            throw UserServiceError(str)
        }
    }
  }
}
