package webmodelica.integrationtests

import webmodelica.services._
import webmodelica.stores._
import webmodelica.models._
import webmodelica.models.mope._
import webmodelica.models.mope.requests._
import webmodelica.models.mope.responses._
import webmodelica.core.AppModule

import java.nio.file._
import better.files._
import com.twitter.finatra.json.modules.FinatraJacksonModule
import com.fasterxml.jackson.databind.ObjectMapper
import com.twitter.finatra.json.FinatraObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.twitter.util.{Future,Await}

class MopeIntegration
    extends webmodelica.WMSpec {
  val conf = AppModule.configProvider.mope
  val session = Session(Project(ProjectRequest("nico", "awesome project")))
  val service = new SessionService(conf,session)

  val files = Seq(
    ModelicaFile(
      Paths.get("a/b/simple.mo"),
      s"""model Simple
|Real x = 5;
|Real y = 10;
|end Simple;
|""".stripMargin
    ),
    ModelicaFile(
      Paths.get("a/square.mo"),
      s"""function Square
|  input Real x;
|  output Real y;
|algorithm
|  y := x*x;
|end Square;
|""".stripMargin
    )
  )
  val errFiles = Seq(files(1),
    ModelicaFile(
      Paths.get("a/b/simple.mo"),
      s"""model Simple
|Real x = 5;
|Real y = "test";
|end Simple;
|""".stripMargin
    )
  )

  println("setup complete, starting tests ...")

  "The MopeSession" should "connect to mope service" in {
    Await.result(service.connect()) shouldBe an[Int]
  }
  it should "generate the error modelica files" in {
    Await.result(service.update(errFiles))
  }
  it should "compile the files" in {
    val errors = Await.result(service.compile(errFiles(1).relativePath))
    errors should not be empty
  }
  it should "generate the files" in {
    Await.result(service.update(files))
  }
  it should "complete symbols" in {
    val completions = Await.result(service.complete(Complete("a/b/simple.mo", FilePosition(1, 5), "Modelica.")))
    completions should not be empty
    completions.size should be >(4)
  }
}
