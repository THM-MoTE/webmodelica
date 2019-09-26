/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.services

import webmodelica.stores.UserStore
import webmodelica.models._
import webmodelica.constants

import com.twitter.util.Future

/** A UserStore that caches calls to the underlying store. */
class UserService(redisProvider: RedisCacheFactory,
                   underlying:UserStore)
    extends UserStore
    with com.typesafe.scalalogging.LazyLogging {

  logger.info("UserService with caching started...")

  val fallback = (k:String) => underlying.findBy(k)
  val cache = redisProvider.get[User](constants.userCacheSuffix, fallback)

  override def add(u:User): Future[Unit] = underlying add u
  override def findBy(username: String): Future[Option[User]] = {
    cache.find(username)
      .handle {
        case _ =>
          logger.warn(s"didn't find a value for $username")
          None
      }
  }
}
