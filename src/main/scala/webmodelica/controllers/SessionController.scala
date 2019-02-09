package webmodelica.controllers

import com.google.inject.Inject
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import org.mongodb.scala.bson.BsonObjectId
import webmodelica.models.JSSession
import webmodelica.services.SessionRegistry
import webmodelica.stores.ProjectStore

class SessionController@Inject()(projectStore:ProjectStore, sessionRegistry: SessionRegistry)
  extends Controller {

  post("/projects/:projectId/session/new") { requ:Request =>
    val id = requ.getParam("projectId")
    for {
      project <- projectStore.findBy(BsonObjectId(id))
    } yield JSSession(sessionRegistry.create(project))
  }
}
