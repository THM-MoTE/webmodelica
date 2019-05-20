package webmodelica

import io.circe._
import java.nio.file.{Path, Paths}
import java.util.UUID

package object models {
  implicit val encodePath: Encoder[Path] = new Encoder[Path] {
    final def apply(a: Path): Json = Json.fromString(a.toString)
  }

  implicit val decodePath: Decoder[Path] = new Decoder[Path] {
    final def apply(c: HCursor): Decoder.Result[Path] =
    c.as[String].map(Paths.get(_))
  }
  implicit val encodeUUID = new Encoder[UUID] {
    final def apply(id: UUID): Json = Json.fromString(id.toString)
  }
  implicit val decodeUUID = new Decoder[UUID] {
    final def apply(c: HCursor): Decoder.Result[UUID] =
      c.as[String].map(UUID.fromString)
  }
}
