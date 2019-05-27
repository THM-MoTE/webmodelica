/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.core

import java.nio.file.Path

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.twitter.finatra.json.modules._
import com.fasterxml.jackson.databind.{JsonSerializer, PropertyNamingStrategy, SerializerProvider}

object JsonConfigModule extends FinatraJacksonModule {
  override val propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE

  val pathSerializer = new JsonSerializer[Path] {
    override def handledType(): Class[Path] = classOf[Path]
    override def serialize(value: Path, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
      gen.writeString(value.toString)
    }
  }

  override val additionalJacksonModules: Seq[SimpleModule] = Seq(
    new SimpleModule { addSerializer(pathSerializer) }
  )
}
