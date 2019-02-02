package webmodelica.models

package config {
  case class WMConfig(
    mope: MopeClientConfig,
    mongodb: MongoDBConfig,
  )

  case class MongoDBConfig(
    address: String
  )

  case class MopeClientConfig(
    address: String
  )
}
