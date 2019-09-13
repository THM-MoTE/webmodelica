/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.models.mope

import webmodelica.utils
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
    parameters: Option[Seq[String]],
    classComment: Option[String],
    `type`: Option[String]) {
    def displayString: String =
      s"$kind - $name"
  }

  @JsonCodec
  case class SimulationResult(modelName:String, variables:Map[String,Seq[Double]]) {
    //trim simulation data to max_value by returning only every n-th element
    def trimmedVariables(size:Int): Map[String,Seq[Double]] = {
      variables.mapValues {
        case timeSeries if timeSeries.size>size => utils.skip(timeSeries, timeSeries.size/size)
        case timeSeries => timeSeries
      }
    }

    override def toString:String = {
      val vars = variables.keys.mkString(",")
      s"SimulationResult(name: $modelName, variables: $vars)"
    }
  }
}
