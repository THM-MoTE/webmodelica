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
import webmodelica.models.errors._
import webmodelica.models.config.RedisConfig
import webmodelica.constants

import com.twitter.util.Future
import com.twitter.cache.FutureCache
import com.twitter.finagle.stats.StatsReceiver
import com.google.inject.Inject
import scala.collection.JavaConverters._

/** A UserStore that caches calls to the underlying store. */
class UserService@Inject()(redisConfig:RedisConfig, statsReceiver:StatsReceiver, underlying:UserStore)
    extends UserStore
    with com.twitter.inject.Logging {

  info("UserService with caching started...")

  val fallback = (k:String) => underlying.findBy(k)
  val cache = new RedisCacheImpl[User](redisConfig, constants.userCacheSuffix, fallback, statsReceiver)

  override def add(u:User): Future[Unit] = underlying add u
  override def findBy(username: String): Future[Option[User]] = {
    cache.find(username)
      .handle {
        case _ =>
          warn(s"didn't find a value for $username")
          None
      }
  }
}
