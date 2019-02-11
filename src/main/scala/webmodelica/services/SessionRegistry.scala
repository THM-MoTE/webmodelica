package webmodelica.services

import com.google.inject.Inject
import com.twitter.finatra.json.FinatraObjectMapper
import com.twitter.util.{Future, FuturePool}
import webmodelica.UUIDStr
import webmodelica.models.config.MopeClientConfig
import webmodelica.models.{Project, Session}

import scala.collection.concurrent

class SessionRegistry @Inject()(conf:MopeClientConfig, json:FinatraObjectMapper)
  extends com.twitter.inject.Logging {

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

  def create(p:Project): Session = sync {
    val s = Session(p)
    info(s"creating session $s")
    registry += (s.idString -> new SessionService(conf, s, json))
    s
  }

  def get(id:UUIDStr): Option[SessionService] = sync{ registry.get(id) }
}
