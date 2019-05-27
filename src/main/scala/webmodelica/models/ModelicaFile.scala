/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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

@JsonCodec
case class ModelicaPath(relativePath: Path)

object ModelicaFile {
  def apply(doc: ModelicaFileDocument): ModelicaFile = doc.into[ModelicaFile].transform
}
