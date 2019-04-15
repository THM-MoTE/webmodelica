package webmodelica.models.mope

import io.circe.generic.JsonCodec

  /** A position (2D Point) inside of a file */
  @JsonCodec
  case class FilePosition(line: Int, column: Int)
