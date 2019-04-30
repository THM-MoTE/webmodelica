package webmodelica.models

import java.nio.file.{Path,Paths}
import pureconfig.ConfigReader
import scala.concurrent.duration.Duration

package config {
  case class WMConfig(
    mope: MopeClientConfig,
    mongodb: MongoDBConfig,
    redis: RedisConfig,
    secret: String,
    tokenExpiration: Duration,
    cacheUsers: Boolean
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

  object configReaders {
    implicit val pathReader:ConfigReader[Path] = ConfigReader[String].map(s => Paths.get(s).toAbsolutePath)
  }
}
