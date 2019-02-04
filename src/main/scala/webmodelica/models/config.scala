package webmodelica.models

import java.nio.file.{Path,Paths}
import pureconfig.ConfigReader

package config {
  case class WMConfig(
    mope: MopeClientConfig,
    mongodb: MongoDBConfig,
  )

  case class MongoDBConfig(
    address: String
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
    implicit val pathReader:ConfigReader[Path] = ConfigReader[String].map(s => Paths.get(s))
  }
}