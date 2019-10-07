package webmodelica

import webmodelica.models._
import java.nio.file.{Path, Paths}

import com.sksamuel.avro4s._
import org.apache.avro._
import org.slf4j.LoggerFactory
import better.files.File
import webmodelica.controllers._
import webmodelica.models.mope._
import webmodelica.models.mope.requests._
import webmodelica.models.mope.responses._
import cats.Applicative
import cats.instances.option._

trait Schemas {
  implicit object pathSchema extends SchemaFor[Path] {
    override def schema(implicit naming: NamingStrategy = DefaultNamingStrategy): Schema = SchemaBuilder.builder.stringType
  }
  implicit def traversableSchema[A:SchemaFor]: SchemaFor[Traversable[A]] = new SchemaFor[Traversable[A]]() {
    override def schema(implicit namingStrategy: NamingStrategy): Schema =
      SchemaBuilder.builder.array().items(AvroSchema[A])
  }
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

  def writeSchemaFor[T](tpe:TpeWrapper[T]): File = {
    val schemaFile = tpe.schemaFile(schemaDirectory)
    log.info(s"writing schema for ${tpe.name} -> $schemaFile")
    schemaFile.overwrite(tpe.jsonSchema)
  }
  def writeExampleFor[T](tpe:TpeWrapper[T]): Option[File] = {
    val fileAndExample = Applicative[Option].product(tpe.exampleFile(exampleDirectory), tpe.jsonExample)
    fileAndExample match {
      case Some((file, example)) =>
        log.info(s"writing example for ${tpe.name} -> $file")
        file.overwrite(example)
        Some(file)
      case _ =>
        log.info(s"no example for ${tpe.name} available!")
        None
    }
  }


  val user = AuthUser("tim", Some("tim@xample.org"), Some("Tim"), Some("Mueller"))
  val projRequest = AkkaProjectController.ProjectRequest(user.username, "awesomeProject")
  val project = Project(projRequest)
  val file = ModelicaFile(Paths.get("a/b/fac.mo"), "model fac\nend fac;")
  val path = ModelicaPath(file.relativePath)
  val models:Seq[TpeWrapper[Any]] = Seq(
    TpeWrapper(AvroSchema[Infos], Infos()),
    TpeWrapper(AvroSchema[AuthUser], user),
    TpeWrapper(AvroSchema[AkkaProjectController.ProjectRequest], projRequest),
    TpeWrapper(AvroSchema[JSProject], JSProject(project)),
    TpeWrapper(AvroSchema[JSSession], JSSession(Session(project), List(path))),
    TpeWrapper(AvroSchema[ModelicaPath], path),
    TpeWrapper(AvroSchema[ModelicaFile], file),
    TpeWrapper(AvroSchema[FilePosition], FilePosition(2,0)),
    TpeWrapper(AvroSchema[FilePath], FilePath("a/b/random.mo")),
    TpeWrapper(AvroSchema[Suggestion], Suggestion("function",
      "random",
      Some(Seq("start", "end")),
      Some("generates random numbers between start & end."),
      Some("real"))),
    TpeWrapper(AvroSchema[CompilerError], CompilerError("Error",
      "a/b/random.mo",
      FilePosition(1,5),
      FilePosition(2,0),
      "incompatible types, expected real found string")),
    TpeWrapper(AvroSchema[Complete], Complete("a/b/random.mo", FilePosition(2,0), "Modelica.Elec")),
    TpeWrapper[String](AvroSchema[SimulationResult], None, None),
    TpeWrapper[String](AvroSchema[TableFormat], None, None),
    TpeWrapper(AvroSchema[Either[ModelicaFile, ModelicaPath]], "FileOrPath"),
    TpeWrapper(AvroSchema[Either[SimulationResult, TableFormat]], "SimulationResultOrTableFormat")
  )

  val buf = scala.collection.mutable.ArrayBuffer(
    """#%RAML 1.0
      |---
    """.stripMargin)
  for(model <- models) {
    val schemaFile = writeSchemaFor(model)
    val exampleFileOpt = writeExampleFor(model)
    val exampleInclude = exampleFileOpt.map { file => s"  example: !include ${apiDirectory relativize file}" }.getOrElse("")
    buf += s"""${model.name}:
      |  type: !include ${apiDirectory relativize schemaFile}
      |${exampleInclude}""".stripMargin
  }
  val tpeRaml = apiDirectory / "types.raml"
  val content = buf.mkString("\n")
  log.info(s"writing into $tpeRaml : $content")
  tpeRaml.overwrite(content)
}
