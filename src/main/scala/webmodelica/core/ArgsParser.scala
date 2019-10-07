/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.core

import org.rogach.scallop._

class ArgsParser(args:Seq[String]) extends ScallopConf(args) {
  version(s"${buildinfo.BuildInfo.name} ${buildinfo.BuildInfo.version} ${buildinfo.BuildInfo.copyright}")
  val interface = opt[String]("interface", default=Some("0.0.0.0"), descr="the binding interface, defaults to `localhost`")
  val port = opt[Int]("port", default=Some(8888), descr="the binding port, defaults to `8888`")
  val env = opt[String]("environment", default=Some("development"), descr="the environment, defaults to `development`")
  val configFile = opt[java.io.File]("configFile", descr="the config file to use")
  validateFileExists(configFile)
  validateFileIsFile(configFile)
  verify()
}
