package webmodelica.models

import webmodelica.WMSpec
import webmodelica.models.mope.requests._
import io.circe.syntax._
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import scala.util._

class SimulateRequestSpec extends WMSpec with ScalaCheckDrivenPropertyChecks {

  "The function 'convertStepSize'" should "convert stepSize to numberOfIntervals" in {
    val options = Map(
      "startTime" -> 0.0,
      "stopTime" -> 5.0,
      "stepSize" -> 0.01
    ).mapValues(_.asJson)
    val req = SimulateRequest("", options)
    val reqTry = req.convertStepSize
    reqTry shouldBe a [Success[_]]
    val newReq = reqTry.get
    newReq.options("numberOfIntervals") shouldBe( (5-0)/0.01 ).asJson
  }
  it should "panic if endTime > startTime" in {
    val options = Map(
      "startTime" -> 5.0,
      "stopTime" -> 0.0,
      "stepSize" -> 0.01
    ).mapValues(_.asJson)
    val req = SimulateRequest("", options)
    val reqTry = req.convertStepSize
    reqTry shouldBe a [Failure[_]]
  }
  it should "panic if 'startTime' or 'endTime' is missing" in {
    val options = Map(
      "startTime" -> 0.0,
      "stepSize" -> 0.01
    ).mapValues(_.asJson)
    val req = SimulateRequest("", options)
    val reqTry = req.convertStepSize
    reqTry shouldBe a [Failure[_]]
  }
  it should "return the same SimulateRequest if not stepSize provided" in {
    val options = Map(
      "startTime" -> 5.0,
      "stopTime" -> 0.0
    ).mapValues(_.asJson)
    val req = SimulateRequest("", options)
    val reqTry = req.convertStepSize
    reqTry.get shouldBe req
  }
  it should "convert the stepSize for whatever input" in {
    forAll(
      Gen.choose(1.0, 1000.0),
      Gen.choose(1.0, 1000.0),
      Gen.choose(1.0, 1000.0)) { (size:Double, start:Double, end:Double) =>
      whenever(start<end) {
        val options = Map(
          "startTime" -> start,
          "stopTime" -> end,
          "stepSize" -> size
        ).mapValues(_.asJson)
        val req = SimulateRequest("", options)
        val intervals = req.convertStepSize.get.options("numberOfIntervals").as[Double].getOrElse(fail("numberOfIntervals should be a double"))
        intervals shouldBe ( (end-start)/size )
      }
    }
  }
}
