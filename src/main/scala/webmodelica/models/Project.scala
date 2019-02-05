package webmodelica.models

import org.mongodb.scala.bson.BsonObjectId

case class Project(
  id: BsonObjectId,
  owner: String,
  name: String,
)
