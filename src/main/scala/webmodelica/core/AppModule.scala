package webmodelica.core

import webmodelica.models.config._
import com.twitter.inject.TwitterModule
import com.google.inject.{
  Singleton,
  Provides
}
import org.mongodb.scala._
import scala.concurrent.ExecutionContext

object AppModule extends TwitterModule with webmodelica.models.DocumentWriters {
  val env = flag(name="env", default="development", help="environment to use")

  @Singleton
  @Provides
  def configProvider: WMConfig = {
    import com.typesafe.config.ConfigFactory
    import webmodelica.models.config.WMConfig
    import webmodelica.models.config.configReaders._
    import pureconfig.generic.auto._
    val rootConfig = ConfigFactory.load("webmodelica.conf")
    pureconfig.loadConfigOrThrow[WMConfig](rootConfig.getConfig(env()))
  }

  @Provides
  def dbConfigProvider(wm:WMConfig): MongoDBConfig = wm.mongodb

  @Singleton
  @Provides
  def mongoProvider(dbConf:MongoDBConfig): MongoDatabase = {
    MongoClient(dbConf.address)
      .getDatabase(dbConf.database)
      .withCodecRegistry(codecRegistry)
  }
}
