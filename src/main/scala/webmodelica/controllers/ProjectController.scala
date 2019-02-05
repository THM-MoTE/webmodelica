package webmodelica.controllers

import com.google.inject.Inject
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import org.mongodb.scala.bson.BsonObjectId
import webmodelica.models.{Project, ProjectRequest}
import webmodelica.models.config.WMConfig
import webmodelica.stores.ProjectStore

class ProjectController@Inject()(store:ProjectStore)
  extends Controller {

  post("/projects") { project:ProjectRequest =>
    logger.debug(s"got project $project")
    val newProj = Project(project)
    store.add(newProj).map(_ => newProj)
  }
}
