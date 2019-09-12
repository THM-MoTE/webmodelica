package webmodelica

import better.files.File
import io.circe.syntax._
import org.apache.avro._
import _root_.io.circe.Encoder
import _root_.io.circe.Json

case class TpeWrapper[+T](schema:Schema,
                          example:Option[T]=None,
                          providedName:Option[String]=None)(implicit encoder:Encoder[T]) {
  def jsonExample:Option[String] = example.map(_.asJson.toString)
  def jsonSchema:String = schema.toString(true)
  def name:String = providedName.getOrElse(schema.getName)
  def fullName:String = providedName.getOrElse(schema.getFullName)
  def exampleFile(dir:File): Option[File] = jsonExample.map { content =>
    dir / s"$fullName-example.json"
  }
  def schemaFile(dir:File): File = dir / s"$fullName-schema.json"
}

object TpeWrapper {
  def apply[T:Encoder](schema:Schema, example:T):TpeWrapper[T] = TpeWrapper(schema, Some(example))
  def apply(schema:Schema, name:String):TpeWrapper[String] = TpeWrapper[String](schema, None, Some(name))
}
