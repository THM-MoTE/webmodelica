package webmodelica.models.mope
import io.circe.generic.JsonCodec

/** mope-server request types. */
object requests {
  /** A description for projects */
  @JsonCodec
  case class ProjectDescription(
    path: String,
    outputDirectory: String="out",
    buildScript: Option[String]=None)

  @JsonCodec
  case class Complete(file: String,
    position: FilePosition,
    word: String)

  @JsonCodec
  case class SimulateRequest(modelName:String, options:Map[String,io.circe.Json])
}
