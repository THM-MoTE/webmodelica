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
  def apply(projectRequest: ProjectRequest): Project = Project(BsonObjectId(), projectRequest.owner, projectRequest.name)
}
