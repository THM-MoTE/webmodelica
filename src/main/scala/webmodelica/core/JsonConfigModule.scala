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
