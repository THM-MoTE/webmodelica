package webmodelica.core

import org.rogach.scallop._

class ArgsParser(args:Seq[String]) extends ScallopConf(args) {
  version(s"${buildinfo.BuildInfo.name} ${buildinfo.BuildInfo.version} ${buildinfo.BuildInfo.copyright}")
  val interface = opt[String]("interface", default=Some("localhost"), descr="the binding interface, defaults to `localhost`")
  val port = opt[Int]("port", default=Some(8888), descr="the binding port, defaults to `8888`")
  val env = opt[String]("environment", default=Some("dev"), descr="the environment, defaults to `dev`")
  val configFile = opt[java.io.File]("configFile", descr="the config file to use")
  validateFileExists(configFile)
  validateFileIsFile(configFile)
  verify()
}
