package webmodelica.services

import com.twitter.util.Future
import scredis.serialization.{Reader, Writer}

import scala.concurrent.{Future => SFuture}
import webmodelica.conversions.futures._

class AsyncRedisCache[A:Reader:Writer](client: scredis.Client,
                                       ttlKeys: scala.concurrent.duration.FiniteDuration,
                                       keySuffix: String,
                                       cacheMiss: String => Future[Option[A]])
  extends RedisCache[A]
  with com.typesafe.scalalogging.LazyLogging {
  import client.dispatcher
  private val ttl = Some(ttlKeys)

  logger.info(s"cache for $keySuffix started (ttl=$ttlKeys)")

  private def makeKey(k:String): String = s"${keySuffix}:${k}"
  private def updateIfEmpty(key:String): Future[Option[A]] =
    cacheMiss(key).flatMap {
      case Some(v) => update(key, v).map(Some.apply)
      case _ => Future.value(None)
    }

  override def find(key: String): Future[Option[A]] =
    client.get(makeKey(key))
      .flatMap {
        case opt@Some(_) =>
          logger.debug(s"found value for $key")
          SFuture.successful(opt)
        case None =>
          logger.debug(s"cache miss for $key")
          cacheMiss(key).asScala
      }
      .asTwitter

  override def update(key: String, value: A): Future[A] =
    client.set(makeKey(key), value, ttl)
      .flatMap { bool =>
        if(bool) {
          logger.debug(s"cached $value at $key")
        } else {
          logger.error(s"Couldn't cache $value in redis")
        }
        SFuture.successful(value)
      }
      .asTwitter
  override def remove(key: String): Future[Unit] =
    client.del(makeKey(key))
      .map { cnt =>
        logger.info(s"removed $cnt values for $key")
        cnt
      }
      .asTwitter
      .unit
}

object AsyncRedisCache {
  import io.circe.{Encoder, Decoder, parser}
  import webmodelica.constants

  implicit def readerFromDecoder[A:Decoder]: Reader[A] = new Reader[A] {
    override protected def readImpl(bytes: Array[Byte]): A = {
      val str = new String(bytes, constants.encoding)
      parser.decode[A](str).toTry.get
    }
  }
  implicit def writerFromEncoder[A:Encoder]: Writer[A] = new Writer[A] {
    override protected def writeImpl(value: A): Array[Byte] =
      implicitly[Encoder[A]].apply(value).noSpaces.getBytes(constants.encoding)
  }

}
