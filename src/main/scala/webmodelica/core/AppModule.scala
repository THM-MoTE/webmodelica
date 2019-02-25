package webmodelica.core

import java.security.MessageDigest

import webmodelica.models.config._
import com.twitter.util.{Await, Time}
import com.twitter.inject.{Injector, TwitterModule}
import com.google.inject.{Provides, Singleton}
import org.mongodb.scala._
import webmodelica.ApiPrefix
import webmodelica.models._
import webmodelica.services._

import scala.concurrent.ExecutionContext

object AppModule
  extends TwitterModule
    with webmodelica.models.DocumentWriters {
  private val confDefault = "webmodelica.conf"
  val env = flag(name="env", default="development", help="environment to use")
  val configFile = flag(name="configFile", default=confDefault, help="the config file to use")

  @Provides
  def prefixProvider: ApiPrefix = ApiPrefix("/api/v1")

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
  def sessionRegistry(conf:MopeClientConfig):SessionRegistry = new SessionRegistry(conf)

  @Provides
  def tokenGenerator(conf:WMConfig): TokenGenerator = new TokenGenerator(conf.secret, conf.tokenExpiration)

  @Provides
  @Singleton
  def digestHasher: MessageDigest = MessageDigest.getInstance("SHA-256")
}
