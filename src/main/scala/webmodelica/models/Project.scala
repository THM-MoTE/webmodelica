package webmodelica.models

import org.mongodb.scala.bson.BsonObjectId
import io.scalaland.chimney.dsl._

case class Project(
  _id: String,
  owner: String,
  name: String,
)

case class ProjectRequest(
  owner: String,
  name: String,
  request: com.twitter.finagle.http.Request)

case class JSProject(
  id: String,
  owner: String,
  name: String)

object JSProject {
  def apply(p:Project): JSProject = {
    require(p != null, "project can't be null!")
    p.into[JSProject].withFieldRenamed(_._id, _.id).transform
  }
}

object Project {
  def apply(request: ProjectRequest): Project =
    request.into[Project].withFieldComputed(_._id, req => s"${req.owner}_${req.name}").transform
}
