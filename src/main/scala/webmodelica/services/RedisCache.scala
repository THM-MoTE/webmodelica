/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.services

import com.twitter.util.Future
import scala.concurrent.duration.FiniteDuration
import io.circe._

trait RedisCache[A] {
  def find(key:String): Future[Option[A]]
  def update(key:String, value:A): Future[A]
  def remove(key:String): Future[Unit]
}

trait RedisCacheFactory {
  def get[A:Encoder:Decoder](keySuffix: String, cacheMiss: String => Future[Option[A]], ttlKeys:Option[FiniteDuration]=None): RedisCache[A]
}

/** Does no caching, just forwards to the given 'fn' */
class NoCaching[A](fn: String => Future[Option[A]]) extends RedisCache[A] {
  def find(key:String): Future[Option[A]] = fn(key)
  def update(key:String, value:A): Future[A] = Future.value(value)
  def remove(key:String): Future[Unit] = Future.value(())
}
