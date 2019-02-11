package webmodelica.models

import java.nio.file.Path
import org.mongodb.scala.bson.BsonObjectId
import io.scalaland.chimney.dsl._

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

object ModelicaFile {
  def apply(doc: ModelicaFileDocument): ModelicaFile = doc.into[ModelicaFile].transform
}
