package webmodelica.services

import com.twitter.finagle.redis._
import com.twitter.finagle.redis.util._
import com.twitter.util.Future
import webmodelica.models.config.RedisConfig
import webmodelica.conversions.futures._
import io.circe._
import io.circe.parser._
import cats.data.OptionT
import cats.implicits._

trait RedisCache[A] {
  def find[A:Decoder](key:String): Future[Option[A]]
  def update[A:Encoder](key:String, value:A): Future[A]
}

class RedisCacheImpl[A](
  config:RedisConfig,
  keySuffix: String,
  cacheMiss: String => Future[Option[A]])
  extends RedisCache[A]
  with com.twitter.inject.Logging  {
  val client = Client(config.address)

  info(s"redis cache for $keySuffix loaded using config: $config")

  private def makeKey(k:String): String = s"${keySuffix}:${k}"

  def find[A:Decoder](key:String): Future[Option[A]] = {
    (for {
      buf <- OptionT(client.get(StringToBuf(makeKey(key))))
      json <- OptionT.liftF(eitherToFuture(parse(BufToString(buf))))
      a <- OptionT.liftF(eitherToFuture(implicitly[Decoder[A]].decodeJson(json)))
    } yield a).value.flatMap {
      case Some(a) =>
        info(s"cache hit for $key")
        Future.value(Some(a))
      case None =>
        info(s"cache miss for $key")
        // cacheMiss(key).map(updateIfAvailable[A](key))
        Future.value(None)
    }
  }

  private def updateIfAvailable[A:Encoder](key:String)(valueOpt:Option[A]): Option[A] = {
    valueOpt.map { v =>
      update(key, v)
      v
    }
  }

  def update[A:Encoder](key:String, value:A): Future[A] = {
    val finalKey = makeKey(key)
    val json = implicitly[Encoder[A]].apply(value).noSpaces
    val keyBuf = StringToBuf(finalKey)
    val valBuf = StringToBuf(json)
    debug(s"adding $json at $finalKey")
    client.setEx(keyBuf, config.defaultTtl.toSeconds, valBuf).map(_ => value)
  }
}
