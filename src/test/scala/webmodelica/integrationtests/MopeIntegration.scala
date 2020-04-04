/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.integrationtests

import webmodelica.services._
import webmodelica.stores._
import webmodelica.models._
import webmodelica.models.mope._
import webmodelica.models.mope.requests._
import webmodelica.models.mope.responses._

import java.nio.file._
import com.twitter.util.{Future,Await}


class MopeIntegration
    extends webmodelica.WMSpec {
  val conf = appConf.mope
  val session = Session(Project("nico", "awesomeProject"))
  val service = new SessionService(conf,session, NoCaching.cacheFactory, module.httpClient)

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
    ),
    ModelicaFile(
      Paths.get("BouncingBall.mo"),
      s"""
// source: http://book.xogeny.com/behavior/discrete/bouncing/

model BouncingBall "The 'classic' bouncing ball model"
  type Height=Real(unit="m");
  type Velocity=Real(unit="m/s");
  parameter Real e=0.8 "Coefficient of restitution";
  parameter Height h0=1.0 "Initial height";
  Height h "Height";
  Velocity v(start=0.0, fixed=true) "Velocity";
initial equation
  h = h0;
equation
  v = der(h);
  der(v) = -9.81;
  when h<0 then
    reinit(v, -e*pre(v));
  end when;
end BouncingBall;
""".stripMargin
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

  it should "simulate and get a location" in {
    import io.circe.syntax._
    Await.result(service.compile(files.last.relativePath))
    Await.result(service.simulate(SimulateRequest("BouncingBall", Map("stopTime" -> 1.asJson))))
  }

  it should "return simulation-results one's completed" in {
    import io.circe.syntax._
    Await.result(service.compile(files.last.relativePath))
    val uri = Await.result(service.simulate(SimulateRequest("BouncingBall", Map("stopTime" -> 2.asJson))))
    Thread.sleep(5000)
    Await.result(service.simulationResults(uri)) shouldBe an[SimulationResult]
  }
}
