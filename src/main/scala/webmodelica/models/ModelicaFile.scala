package webmodelica.models

import java.nio.file.Path
import org.mongodb.scala.bson.BsonObjectId
import io.scalaland.chimney.dsl._
import io.circe.generic.JsonCodec
import webmodelica._

case class ModelicaFileDocument (
  id: BsonObjectId,
  projectID: BsonObjectId,
  relativePath: Path,
  content: String,
)


@JsonCodec
case class ModelicaFile (
  relativePath: Path,
  content: String,
)

object ModelicaFile {
  def apply(doc: ModelicaFileDocument): ModelicaFile = doc.into[ModelicaFile].transform
}
