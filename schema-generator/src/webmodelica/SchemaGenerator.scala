package webmodelica

import webmodelica.models._
import java.nio.file.Path

import com.sksamuel.avro4s._
import org.apache.avro._
import org.slf4j.LoggerFactory
import better.files.File
import better.files.File.OpenOptions
import com.twitter.finagle.http.Request
import webmodelica.controllers._
import webmodelica.models.mope.requests.SimulateRequest
import webmodelica.models.mope.responses.{SimulationResult, Suggestion}

trait Schemas {
  implicit object pathSchema extends SchemaFor[Path] {
    override def schema(implicit naming: NamingStrategy = DefaultNamingStrategy): Schema = SchemaBuilder.builder.stringType
  }
  implicit object requestSchema extends SchemaFor[Request] {
    override def schema(implicit naming: NamingStrategy = DefaultNamingStrategy): Schema = SchemaBuilder.builder.nullType()
  }
}

object SchemaGenerator
    extends App
    with Schemas {
  val log = LoggerFactory.getLogger("SchemaGenerator")
  log.info(s"schema generator started..")
  val schemaDirectory = File("./doc/api/schemas")
  schemaDirectory.createDirectoryIfNotExists()

  def writeToFile(schema:Schema): Unit = {
    val file = schemaDirectory / (schema.getFullName+".json")
    log.info(s"writing ${schema.getName} -> ${file}")
    file.overwrite(schema.toString(true))
  }

  val schemas = Seq(
    AvroSchema[Infos],
    AvroSchema[AuthUser],
    AvroSchema[ModelicaFile],
    AvroSchema[ProjectRequest],
    AvroSchema[JSProject],
    AvroSchema[JSSession],
    AvroSchema[Suggestion],
    AvroSchema[SimulationResult],
  )

  schemas.foreach(writeToFile)
}
