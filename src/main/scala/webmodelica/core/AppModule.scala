package webmodelica.core

import webmodelica.models.config.WMConfig
import com.twitter.inject.TwitterModule
import com.google.inject.{
  Singleton,
  Provides
}

object AppModule extends TwitterModule {
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
}
