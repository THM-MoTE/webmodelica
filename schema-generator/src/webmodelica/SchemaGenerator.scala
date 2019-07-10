package webmodelica

import webmodelica.models._
import java.nio.file.{
  Path, Paths
}

import io.circe
import io.circe.syntax._
import com.sksamuel.avro4s._
import org.apache.avro._
import org.slf4j.LoggerFactory
import better.files.File
import better.files.File.OpenOptions
import com.twitter.finagle.http.Request
import webmodelica.controllers._
import webmodelica.models.mope._
import webmodelica.models.mope.requests._
import webmodelica.models.mope.responses._

trait Schemas {
  implicit object pathSchema extends SchemaFor[Path] {
    override def schema(implicit naming: NamingStrategy = DefaultNamingStrategy): Schema = SchemaBuilder.builder.stringType
  }
  implicit object requestSchema extends SchemaFor[Request] {
    override def schema(implicit naming: NamingStrategy = DefaultNamingStrategy): Schema = SchemaBuilder.builder.nullType()
  }
}

case class TpeWrapper[+T](value:T)(implicit tag:scala.reflect.ClassTag[T], encoder:circe.Encoder[T]) {
  def json:String = value.asJson.toString
  def fullName:String = tag.runtimeClass.getCanonicalName
  def name:String = tag.runtimeClass.getSimpleName
}

object SchemaGenerator
    extends App
    with Schemas {
  val log = LoggerFactory.getLogger("SchemaGenerator")
  log.info(s"schema generator started..")
  val apiDirectory = File("./doc/api")
  val schemaDirectory = apiDirectory / "schemas"
  val exampleDirectory = apiDirectory / "examples"
  schemaDirectory.createDirectoryIfNotExists()
  exampleDirectory.createDirectoryIfNotExists()

  def writeToFile(schema:Schema): File = {
    val file = schemaDirectory / (schema.getFullName+".json")
    log.info(s"writing ${schema.getName} -> ${file}")
    file.overwrite(schema.toString(true))
    file
  }

  def generateRaml(schemas: Seq[(Schema, File)], examples:Map[String, File]): File = {
    val file = apiDirectory / "types.raml"
    val line = for((schema, schemaFile) <- schemas) yield {
      val path = apiDirectory relativize schemaFile
      val typeStr = s"type: !include ${path}"
      val exampleStr = examples.get(schema.getName)
        .map(apiDirectory.relativize)
        .map(path => s"example: !include ${path}")
      .getOrElse("")
      s"""${schema.getName}:
|  ${typeStr}
|  ${exampleStr}""".stripMargin
    }
    log.info(s"generating $file")
    val content = line.mkString("\n")
    log.debug(s"$file content: $content")
    file.overwrite(content)
    file
  }

  def generateExample[T](model:TpeWrapper[T]): File = {
    val file = exampleDirectory / (model.fullName+"-example.json")
    log.info(s"writing example for ${model.name} -> ${file}")
    file.overwrite(model.json)
  }
  def generateExampleRaml[T](examples:Seq[(TpeWrapper[T], File)]): File = {
    val file = apiDirectory / "examples.raml"
    val line = for((model, exampleFile) <- examples) yield {
      val path = apiDirectory relativize exampleFile
      s"${model.name}: !include ${path}"
    }
    log.info(s"generating $file")
    val content = line.mkString("\n")
    log.debug(s"$file content: $content")
    file.overwrite(content)
    file
  }

  val schemas = Seq(
    AvroSchema[Infos],
    AvroSchema[AuthUser],
    AvroSchema[ModelicaFile],
    AvroSchema[ModelicaPath],
    AvroSchema[ProjectRequest],
    AvroSchema[JSProject],
    AvroSchema[JSSession],
    AvroSchema[Suggestion],
    AvroSchema[SimulationResult],
    AvroSchema[FilePath],
    AvroSchema[FilePosition],
    AvroSchema[CompilerError],
    AvroSchema[Complete],
  )

  log.info("generating types..")
  val files = schemas.map(schema => (schema, writeToFile(schema)))

  log.info("generating examples..")
  import _root_.io.circe._, _root_.io.circe.generic.semiauto._
  val user = AuthUser("tim", Some("tim@xample.org"), Some("Tim"), Some("Mueller"))
  val projRequest = ProjectRequest(user.username, "awesomeProject", Request())
  val project = Project(projRequest)
  val file = ModelicaFile(Paths.get("a/b/fac.mo"), "model fac\nend fac;")
  val path = ModelicaPath(file.relativePath)
  val models:Seq[TpeWrapper[Any]] = Seq(
    TpeWrapper(Infos()),
    TpeWrapper(user),
    TpeWrapper(projRequest),
    TpeWrapper(JSProject(project)),
    TpeWrapper(JSSession(Session(project), List(path))),
    TpeWrapper(path),
    TpeWrapper(file),
    TpeWrapper(FilePosition(2,0)),
    TpeWrapper(FilePath("a/b/random.mo")),
    TpeWrapper(Suggestion("function",
      "random",
      Some(Seq("start", "end")),
      Some("generates random numbers between start & end."),
      Some("real"))),
    TpeWrapper(CompilerError("Error",
      "a/b/random.mo",
      FilePosition(1,5),
      FilePosition(2,0),
      "incompatible types, expected real found string")),
    TpeWrapper(Complete("a/b/random.mo", FilePosition(2,0), "Modelica.Elec")),
  )

  val exampleFiles = models.map(model => (model.name, generateExample(model))).toMap
  generateRaml(files, exampleFiles)
  // generateExampleRaml(exampleFiles)
}
