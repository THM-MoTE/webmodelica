package webmodelica.controllers

import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives._
import akka.http.scaladsl.model._
import java.io.File

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
  def combineAll(ctrls: Traversable[AkkaController]): AkkaController =
    ctrls.reduce {
      (acc, elem) => acc || elem
    }
}
