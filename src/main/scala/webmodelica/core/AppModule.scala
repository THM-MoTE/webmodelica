/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.core

import java.nio.file.Paths
import java.security.MessageDigest

import webmodelica.models.config._
import com.twitter.util.{Await, Time}
import com.twitter.inject.{Injector, TwitterModule}
import com.google.inject.{Provides, Singleton}
import com.twitter.finagle.stats.StatsReceiver
import org.mongodb.scala._
import webmodelica.ApiPrefix
import webmodelica.controllers.JwtFilter
import webmodelica.models._
import webmodelica.services._
import webmodelica.stores._

import scala.concurrent.ExecutionContext

/** Main application wiring.
 * This module configures all services and dependencies for Webmodelica.
 */
object AppModule
  extends TwitterModule
    with webmodelica.models.DocumentWriters {

  private val confDefault = "webmodelica.conf"
  val env = flag(name="env", default="development", help="environment to use")
  val configFile = flag(name="configFile", default=confDefault, help="the config file to use")

  @Provides
  def prefixProvider: ApiPrefix = ApiPrefix("/api/v1/webmodelica")

  override def singletonStartup(injector: Injector) {
    super.singletonStartup(injector)
    // initialize JVM-wide resources
    val _ = injector.instance(classOf[MongoDatabase])
  }

  override def singletonShutdown(injector: Injector): Unit = {
    super.singletonShutdown(injector)
    println("!!! SHUTDOWN CALLED")
    injector.instance(classOf[MongoClient]).close()
    Await.result(injector.instance(classOf[SessionRegistry]).close(Time.fromSeconds(90)))
  }

  @Singleton
  @Provides
  def configProvider: WMConfig = {
    import com.typesafe.config.ConfigFactory
    import webmodelica.models.config.WMConfig
    import webmodelica.models.config.configReaders._
    import pureconfig.generic.auto._
    val rootConfig =
      if(configFile() == confDefault) ConfigFactory.load("webmodelica.conf")
      else ConfigFactory.parseFile(new java.io.File(configFile()))
    val conf = pureconfig.loadConfigOrThrow[WMConfig](rootConfig.getConfig(env()))
    logger.info(s"config loaded: $conf")
    conf
  }

  @Provides
  def dbConfigProvider(wm:WMConfig): MongoDBConfig = wm.mongodb
  @Provides
  def mopeConfigProvider(wm:WMConfig): MopeClientConfig = wm.mope
  @Provides
  def jwtConfProvider(wm:WMConfig): JwtConf = wm.jwtConf
  @Singleton
  @Provides
  def mongoClientProvider(dbConf:MongoDBConfig): MongoClient = MongoClient(dbConf.address)
  @Singleton
  @Provides
  def mongoDBProvider(dbConf:MongoDBConfig, client:MongoClient): MongoDatabase = {
    client.getDatabase(dbConf.database)
      .withCodecRegistry(codecRegistry)
  }

  @Singleton
  @Provides
  def userStore(db:MongoDatabase, config:WMConfig, statsReceiver:StatsReceiver):UserStore = {
    val store = new UserServiceProxy(config.userService)
    if(config.cacheUsers) {
      info(s"caching users enabled")
      new UserService(config.redis, statsReceiver, store)
    } else {
      info(s"caching users disabled")
      store
    }
  }

  @Singleton
  @Provides
  def sessionRegistry(conf:WMConfig, statsReceiver:StatsReceiver):SessionRegistry = {
    if(conf.redisSessions) {
      info(s"save sessions into redis")
      new RedisSessionRegistry(conf, statsReceiver)
    } else {
      info(s"save sessions inmemory")
      new InMemorySessionRegistry(conf, statsReceiver)
    }
  }
  @Provides
  def tokenGenerator(conf:JwtConf): TokenGenerator =
    new TokenGenerator(conf.secret, conf.tokenExpiration)
  @Provides
  def tokenValidator(conf:JwtConf): TokenValidator =
    TokenValidator.combine(tokenGenerator(conf), AuthTokenValidator(KeyFile(conf.authSvcPublicKey)))

  @Provides
  @Singleton
  def digestHasher: MessageDigest = MessageDigest.getInstance("SHA-256")
}
