package webmodelica.core

import webmodelica.models.config._
import com.twitter.inject.{Injector, TwitterModule}
import com.google.inject.{Provides, Singleton}
import org.mongodb.scala._
import webmodelica.models._
import scala.concurrent.ExecutionContext

object AppModule
  extends TwitterModule
    with webmodelica.models.DocumentWriters {
  val env = flag(name="env", default="development", help="environment to use")

  override def singletonStartup(injector: Injector) {
    super.singletonStartup(injector)
    // initialize JVM-wide resources
    val _ = injector.instance(classOf[MongoDatabase])
  }

  override def singletonShutdown(injector: Injector): Unit = {
    super.singletonShutdown(injector)
    println("!!! SHUTDOWN CALLED")
    injector.instance(classOf[MongoClient]).close()
  }

  @Singleton
  @Provides
  def configProvider: WMConfig = {
    import com.typesafe.config.ConfigFactory
    import webmodelica.models.config.WMConfig
    import webmodelica.models.config.configReaders._
    import pureconfig.generic.auto._
    val rootConfig = ConfigFactory.load("webmodelica.conf")
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
    println("!!!! MONGODB PROVIDER CALLED")
    client.getDatabase(dbConf.database)
      .withCodecRegistry(codecRegistry)
  }

  @Provides
  def session:Session = Session(Project(ProjectRequest("nico", "awesome title")))
}
