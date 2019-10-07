/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.services

import com.twitter.util.{Future, FuturePool, Time}
import com.twitter.finagle.stats.StatsReceiver
import webmodelica.UUIDStr
import webmodelica.models.config.WMConfig
import webmodelica.models.{Project, Session}

import scala.collection.concurrent

trait SessionRegistry extends com.twitter.util.Closable {
  def create(p:Project): Future[(SessionService, Session)]
  def get(id:UUIDStr): Future[Option[SessionService]]
  def killSession(id:UUIDStr): Future[Unit]
}

class InMemorySessionRegistry(conf:WMConfig,
  statsReceiver:StatsReceiver,
   client: AkkaHttpClient)
  extends SessionRegistry
    with com.typesafe.scalalogging.LazyLogging
    with com.twitter.util.Closable {

  private val lock:java.util.concurrent.locks.Lock = new java.util.concurrent.locks.ReentrantLock()
  private val registry = concurrent.TrieMap[UUIDStr, SessionService]()

  private def sync[A](f: => A): A = {
    try {
      lock.lock()
      f
    } finally {
      lock.unlock()
    }
  }

  override def create(p:Project): Future[(SessionService, Session)] = FuturePool.unboundedPool { sync {
    val s = Session(p)
    logger.info(s"creating session $s")
    val service = new SessionService(conf.mope, s, conf.redis, statsReceiver, client)
    registry += (s.idString -> service)
    (service, s)
  }}

  override def get(id:UUIDStr): Future[Option[SessionService]] = FuturePool.unboundedPool { sync{ registry.get(id) } }

  override def killSession(id:UUIDStr): Future[Unit] = {
    sync { registry.remove(id) } match {
      case Some(service) =>
        logger.info(s"killing session $id")
        service.close(Time.fromSeconds(60))
      case None =>
        logger.warn(s"session $id not found, we aren't killing it.")
        Future.value(())
    }
  }

  override def close(deadline:Time):Future[Unit] =
    Future.collect(this.registry.values.map(_.close(deadline)).toList).map(_ => ())
}
