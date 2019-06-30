/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.core

object WMServerMain extends WMServer

class WMServer {
  def main(mainArgs: Array[String]) = {
    val module = new WebmodelicaModule {
      override def arguments:Seq[String] = mainArgs
    }
    println(module.config)
    bootstrap(module)
  }

  def bootstrap(module:WebmodelicaModule): Unit = {
    import module._
  }
}
