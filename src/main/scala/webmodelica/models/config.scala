package webmodelica.models

import java.nio.file.{Path,Paths}
import pureconfig.ConfigReader

package config {
  case class WMConfig(
    mope: MopeClientConfig,
    mongodb: MongoDBConfig,
    secret: String,
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
