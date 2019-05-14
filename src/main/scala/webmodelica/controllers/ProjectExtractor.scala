package webmodelica.controllers

import com.twitter.util.Future
import webmodelica.models.{Project, errors}
import webmodelica.stores.ProjectStore

trait ProjectExtractor {
  def projectStore: ProjectStore

  def extractProject(id:String, username:String): Future[Project] =
    projectStore.findBy(id, username).flatMap(errors.notFoundExc(s"project with $id not found!"))
}
