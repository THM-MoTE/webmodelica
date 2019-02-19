package webmodelica.controllers

import com.google.inject.Inject
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import org.mongodb.scala.bson.BsonObjectId
import webmodelica.models.{JSProject, Project, ProjectRequest, errors}
import webmodelica.models.config.WMConfig
import webmodelica.stores.ProjectStore

class ProjectController@Inject()(store:ProjectStore)
  extends Controller {

  filter[JwtFilter]
  .post("/projects") { project:ProjectRequest =>
    logger.debug(s"got project $project")
    val newProj = Project(project)
    store.add(newProj).map(_ => JSProject(newProj))
  }

  filter[JwtFilter]
  .get("/projects/:id") { requ:Request =>
    val id = requ.getParam("id")
    store.findBy(BsonObjectId(id))
      .flatMap(errors.notFoundExc(s"project with $id not found!"))
      .map(JSProject.apply)
  }

  filter[JwtFilter]
  .get("/projects") { _: Request =>
    store.all().map(_.map(JSProject.apply))
  }
}
