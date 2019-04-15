package webmodelica.models.mope

import io.circe.generic.JsonCodec

/** mope-server response types. */
object responses {
  @JsonCodec
  case class CompilerError(
    `type`: String,
    file: String,
    start: FilePosition,
    end: FilePosition,
    message: String)

  object Kind extends Enumeration {
    val Type, Variable, Function, Keyword, Package, Model, Class, Property = Value
  }

  @JsonCodec
  case class Suggestion(kind: String,
    name: String,
    parameters: Option[Seq[String]]=None,
    classComment: Option[String]=None,
    `type`: Option[String]=None) {
    def displayString: String =
      s"$kind - $name"
  }

  @JsonCodec
  case class SimulationResult(modelName:String, variables:Map[String,Seq[Double]])
}
