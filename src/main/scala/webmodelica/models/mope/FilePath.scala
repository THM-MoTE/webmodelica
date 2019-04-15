package webmodelica.models.mope

import io.circe.generic.JsonCodec

/** Wrapper around a path to a file. */
  @JsonCodec
  case class FilePath(path: String) extends AnyVal
