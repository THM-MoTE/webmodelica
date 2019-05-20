package webmodelica.models

import java.nio.file.{Path,Paths}
import pureconfig.ConfigReader
import scala.concurrent.duration.Duration

package config {
  case class WMConfig(
    mope: MopeClientConfig,
    mongodb: MongoDBConfig,
    redis: RedisConfig,
    userService:UserServiceConf,
    jwtConf:JwtConf,
    cacheUsers: Boolean,
    redisSessions:Boolean
  )

  case class MongoDBConfig(
    address: String,
    database: String,
  )

  case class MopeClientConfig(
    address: String,
    data: MopeDataConfig
  )
  case class MopeDataConfig(
    hostDirectory: Path,
    bindDirectory: Path
  )
  case class RedisConfig(
    address: String,
    defaultTtl: Duration
  )

  case class UserServiceConf(
    address: String,
    resource: String)

  case class JwtConf(secret: String,
                    tokenExpiration: Duration,
                     authSvcPublicKey:Path)

  object configReaders {
    implicit val pathReader:ConfigReader[Path] = ConfigReader[String].map(s => Paths.get(s).toAbsolutePath)
  }
}
