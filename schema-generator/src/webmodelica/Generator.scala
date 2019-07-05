package webmodelica

import webmodelica.models.{
  JSProject,
  ModelicaFile
}

import com.sksamuel.avro4s.schema._
import com.sksamuel.avro4s._
import java.nio.file.Path

trait Schemas {
  implicit object pathSchema extends SchemaFor[Path] {
    override def schema(implicit naming: NamingStrategy = DefaultNamingStrategy): Schema = SchemaBuilder.builder.stringType
  }
}

object Generator
    extends App
    with Schemas {
  println("hello from generator ..")

  println(AvroSchema[ModelicaFile])
  println(AvroSchema[JSProject])
}
