package webmodelica.services

import com.twitter.util.Future
import scredis.serialization.{Reader, Writer}

import scala.concurrent.{Future => SFuture}
import webmodelica.conversions.futures._

/** A RedisCache implementation based on an scredis.Client.
  *  It requires an implicit scredis.{Reader, Writer} to be in scope in order to serialize `A` values.
  *  Should be instantiated through companion object's `apply` method.
  */
class AsyncRedisCache[A:Reader:Writer] private (client: scredis.Client,
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

  override def find(key: String): Future[Option[A]] = {
    val mKey = makeKey(key)
    client.get(mKey)
      .flatMap {
        case opt@Some(_) =>
          logger.debug(s"found value for $mKey")
          SFuture.successful(opt)
        case None =>
          logger.debug(s"cache miss for $mKey")
          cacheMiss(key).asScala
      }
      .asTwitter
  }

  override def update(key: String, value: A): Future[A] = {
    val mKey = makeKey(key)
    client.set(mKey, value, ttl)
      .flatMap { bool =>
        if(bool) {
          logger.debug(s"cached $value at $mKey")
        } else {
          logger.error(s"Couldn't cache $value in redis")
        }
        SFuture.successful(value)
      }
      .asTwitter
  }

  override def remove(key: String): Future[Unit] = {
    val mKey = makeKey(key)
    client.del(mKey)
      .map { cnt =>
        logger.info(s"removed $cnt values for $mKey")
        cnt
      }
      .asTwitter
      .unit
  }
}

/** AsyncRedisCache factory and scredis.{Reader,Writer} provider. */
object AsyncRedisCache {
  import io.circe.{Encoder, Decoder, parser}
  import webmodelica.constants

  /** builds a scredis.Reader from an implicit circe.Decoder */
  implicit def readerFromDecoder[A:Decoder]: Reader[A] = new Reader[A] {
    override protected def readImpl(bytes: Array[Byte]): A = {
      val str = new String(bytes, constants.encoding)
      parser.decode[A](str).toTry.get
    }
  }
  /** builds a scredis.Writer from an implicit circe.Encoder */
  implicit def writerFromEncoder[A:Encoder]: Writer[A] = new Writer[A] {
    override protected def writeImpl(value: A): Array[Byte] =
      implicitly[Encoder[A]].apply(value).noSpaces.getBytes(constants.encoding)
  }

  /** Creates an AsyncRedisCache with serializers based on the implicit circe.{Encoder, Decoder}.
    *  This is the only way to obtain an AsyncRedisCache!
    */
  def apply[A:Encoder:Decoder](client: scredis.Client,
            ttlKeys: scala.concurrent.duration.FiniteDuration,
            keySuffix: String,
            cacheMiss: String => Future[Option[A]]): AsyncRedisCache[A] =
    new AsyncRedisCache[A](client, ttlKeys, keySuffix, cacheMiss)
}
