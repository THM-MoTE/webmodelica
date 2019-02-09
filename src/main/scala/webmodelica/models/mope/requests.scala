package webmodelica.models.mope

/** mope-server request types. */
object requests {
  /** A description for projects */
  case class ProjectDescription(
    path: String,
    outputDirectory: String,
    buildScript: Option[String])

  case class Complete(file: String,
    position: FilePosition,
    word: String)
}
