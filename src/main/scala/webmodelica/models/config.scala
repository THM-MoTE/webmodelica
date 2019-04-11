package webmodelica.models

import java.nio.file.{Path,Paths}
import pureconfig.ConfigReader
import scala.concurrent.duration.Duration

package config {
  case class WMConfig(
    mope: MopeClientConfig,
    mongodb: MongoDBConfig,
    secret: String,
    tokenExpiration: Duration,
    cacheUsers:Boolean
  )

  case class MongoDBConfig(
    address: String,
    database:String,
  )

  case class MopeClientConfig(
    address: String,
    data: MopeDataConfig
  )
  case class MopeDataConfig(
    hostDirectory: Path,
    bindDirectory: Path
  )

  object configReaders {
    implicit val pathReader:ConfigReader[Path] = ConfigReader[String].map(s => Paths.get(s).toAbsolutePath)
  }
}
