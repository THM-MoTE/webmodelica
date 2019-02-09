package webmodelica.models.mope

/** mope-server response types. */
object responses {
  case class CompilerError(
    `type`: String,
    file: String,
    start: FilePosition,
    end: FilePosition,
    message: String)

  object Kind extends Enumeration {
    val Type, Variable, Function, Keyword, Package, Model, Class, Property = Value
  }

  case class Suggestion(kind: Kind.Value,
    name: String,
    parameters: Option[Seq[String]],
    classComment: Option[String],
    `type`: Option[String]) {
    def displayString: String =
      s"$kind - $name"
  }
}
