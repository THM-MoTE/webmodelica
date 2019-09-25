/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.controllers

import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import webmodelica.models.Infos

object AkkaInfoController
    extends AkkaController {
  val info = Infos()

  override val routes:Route = (path("info") & get & pathEnd) {
    complete(info)
  }
}
