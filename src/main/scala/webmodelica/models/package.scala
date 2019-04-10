package webmodelica

import io.circe._
import java.nio.file.{Path, Paths}

package object models {
  implicit val encodeFoo: Encoder[Path] = new Encoder[Path] {
    final def apply(a: Path): Json = Json.fromString(a.toString)
  }

  implicit val decodeFoo: Decoder[Path] = new Decoder[Path] {
    final def apply(c: HCursor): Decoder.Result[Path] =
    c.as[String].map(Paths.get(_))
  }
}
