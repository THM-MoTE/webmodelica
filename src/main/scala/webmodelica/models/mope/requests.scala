package webmodelica.models.mope

/** mope-server request types. */
object requests {
  /** A description for projects */
  case class ProjectDescription(
    path: String,
    outputDirectory: String="out",
    buildScript: Option[String]=None)

  case class Complete(file: String,
    position: FilePosition,
    word: String)

  case class SimulateRequest(modelName:String, options:Map[String,io.circe.Json])
}
