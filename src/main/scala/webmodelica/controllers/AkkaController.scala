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
import akka.http.scaladsl.server.directives._
import akka.http.scaladsl.model._
import java.io.File
import io.circe.generic.JsonCodec

trait AkkaController {
  self =>
  def routes: Route
  def ||(other:AkkaController): AkkaController = new AkkaController() {
    //gotcha: don't use 'this' if you realy mean the OUTER object .. !
    override def routes: Route = self.routes ~ other.routes
  }

  def tempDestination(fileInfo: FileInfo): File =
    File.createTempFile(fileInfo.fileName, ".tmp")
}

object AkkaController {
  @JsonCodec
  case class ErrorResponse(errors:Seq[String])

  def combineAll(ctrls: Traversable[AkkaController]): AkkaController =
    ctrls.reduce {
      (acc, elem) => acc || elem
    }
}
