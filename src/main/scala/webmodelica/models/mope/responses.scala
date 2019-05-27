/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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
