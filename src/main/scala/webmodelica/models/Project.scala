package webmodelica.models

import reactivemongo.bson.BSONObjectID

case class Project(
  id: BSONObjectID,
  owner: String,
  name: String,
)
