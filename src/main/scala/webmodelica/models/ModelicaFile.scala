package webmodelica.models

import java.nio.file.Path
import org.mongodb.scala.bson.BsonObjectId

case class ModelicaFileDocument (
  id: BsonObjectId,
  projectID: BsonObjectId,
  relativePath: Path,
  content: String,
)

case class ModelicaFile (
  relativePath: Path,
  content: String,
)
