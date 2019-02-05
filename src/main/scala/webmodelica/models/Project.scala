package webmodelica.models

import org.mongodb.scala.bson.BsonObjectId

case class Project(
  id: BsonObjectId,
  owner: String,
  name: String,
)

case class ProjectRequest(
  owner: String,
  name: String)

object Project {
  import io.scalaland.chimney.dsl._
  def apply(request: ProjectRequest): Project =
    request.into[Project].withFieldComputed(_.id, _ => BsonObjectId()).transform
}
