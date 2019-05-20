package webmodelica.services

import com.google.inject.Inject
import com.twitter.finatra.json.FinatraObjectMapper
import com.twitter.util.{Future, FuturePool, Time}
import com.twitter.finagle.stats.StatsReceiver
import webmodelica.UUIDStr
import webmodelica.models.config._
import webmodelica.models.{Project, Session}
import webmodelica.conversions.futures._
import scala.concurrent.duration._
import cats.data.OptionT
import cats.implicits._

class RedisSessionRegistry(
  conf:WMConfig,
  statsReceiver:StatsReceiver)
    extends SessionRegistry
    with com.twitter.inject.Logging {

  val higherTtlConf = conf.redis.copy(defaultTtl = 8 hours)
  val redis = new RedisCacheImpl[Session](higherTtlConf, "sessions", _ => Future.value(None), statsReceiver)

  private def newService(s:Session): SessionService =
    s.mopeId match {
      case None => new SessionService(conf.mope, s, conf.redis, statsReceiver)
      case Some(id) =>
        new SessionService(conf.mope, s, conf.redis, statsReceiver) {
          override def projectId: Future[Int] = Future.value(id)
        }
    }

  override def create(p:Project): Future[(SessionService, Session)] = {
    //tmp set mopeId to unknown
    val tmpSession = Session(p, mopeId=None)
    val service = newService(tmpSession)
    for {
      mopeId <- service.connect()
      session = tmpSession.copy(mopeId=mopeId.some)
      _ <- redis.update(session.idString, session)
    } yield (service, session)
  }
  override def get(id:UUIDStr): Future[Option[SessionService]] =
    OptionT(redis.find(id)).map(newService).value
  override def killSession(id:UUIDStr): Future[Unit] = Future.value(())

  override def close(deadline:Time):Future[Unit] = Future.value(())
}
