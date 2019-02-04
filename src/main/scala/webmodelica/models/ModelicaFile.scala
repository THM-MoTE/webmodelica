package webmodelica.models

import java.nio.file.Path
import reactivemongo.bson.BSONObjectID

case class ModelicaFileDocument (
  id: BSONObjectID,
  projectID: BSONObjectID,
  relativePath: Path,
  content: String,
)

case class ModelicaFile (
  relativePath: Path,
  content: String,
)
