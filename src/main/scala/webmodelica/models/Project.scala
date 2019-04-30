package webmodelica.models

import org.mongodb.scala.bson.BsonObjectId
import io.scalaland.chimney.dsl._
import io.circe.generic.JsonCodec

case class Project(
  _id: String,
  owner: String,
  name: String,
  visibility:String = Project.privateVisibility
)

case class ProjectRequest(
  owner: String,
  name: String,
  request: com.twitter.finagle.http.Request)

@JsonCodec
case class JSProject(
  id: String,
  owner: String,
  name: String,
  visibility:String)

object JSProject {
  def apply(p:Project): JSProject = {
    require(p != null, "project can't be null!")
    p.into[JSProject].withFieldRenamed(_._id, _.id).transform
  }
}

object Project {

  val publicVisibility = "public"
  val privateVisibility = "private"

  def apply(request: ProjectRequest): Project =
    request.into[Project]
      .withFieldComputed(_._id, req => s"${req.owner}_${req.name}")
      .withFieldComputed(_.visibility, _ => privateVisibility)
      .transform
}
