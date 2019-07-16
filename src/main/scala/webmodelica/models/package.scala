/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica

import io.circe._
import io.circe.syntax._
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
  implicit val encodeRequest = new Encoder[ProjectRequest] {
    final def apply(proj:ProjectRequest): Json = JSProject(Project(proj)).asJson
  }
}
