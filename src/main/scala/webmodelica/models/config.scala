/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.models

import java.nio.file.{Path,Paths}
import pureconfig.ConfigReader
import scala.concurrent.duration.Duration

package config {
  case class MaxSimulationData(value:Int) extends AnyVal
  case class WMConfig(
    mope: MopeClientConfig,
    mongodb: MongoDBConfig,
    redis: RedisConfig,
    userService:UserServiceConf,
    jwtConf:JwtConf,
    cacheUsers: Boolean,
    redisSessions:Boolean,
    maxSimulationData:MaxSimulationData
  )

  case class MongoDBConfig(
    address: String,
    database: String,
  )

  case class MopeClientConfig(
    address: String,
    clientResponseSize: Int,
    data: MopeDataConfig
  )
  case class MopeDataConfig(
    hostDirectory: Path,
    bindDirectory: Path
  )
  case class RedisConfig(
    host: String,
    port: Int,
    defaultTtl: Duration
  ) {
    def address: String = s"$host:$port"
  }

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
