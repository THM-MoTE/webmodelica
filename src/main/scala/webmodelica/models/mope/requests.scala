package webmodelica.models.mope
import io.circe.generic.JsonCodec
import webmodelica.models.errors.StepSizeCalculationError

import scala.util.{Failure, Success, Try}

/** mope-server request types. */
object requests {
  /** A description for projects */
  @JsonCodec
  case class ProjectDescription(
    path: String,
    outputDirectory: String="out",
    buildScript: Option[String]=None)

  @JsonCodec
  case class Complete(file: String,
    position: FilePosition,
    word: String)

  @JsonCodec
  case class SimulateRequest(modelName:String, options:Map[String,io.circe.Json]) {
    def convertStepSize: Try[SimulateRequest] = {
      import cats._ , cats.instances.OptionInstances , cats.implicits._
      import io.circe.syntax._
      val optionalTry = (options.get("stepSize"), options.get("startTime"), options.get("stopTime")).mapN {
        (stepSize, startTime, stopTime) =>
          /* convert stepSize to numberOfIntervals
            stepSize = (stopTime - startTime) / numberOfIntervals
            numberOfIntervals = (stopTime - startTime) / stepSize
           */
          for {
            size <- stepSize.as[Double].toTry
            start <- startTime.as[Double].toTry
            end <- stopTime.as[Double].toTry
            if end >= start
          } yield (start-end) / size
      }

      //if there is a stepSize provided, conversion should be working; otherwise return this
      if(options contains "stepSize") {
        val innerTry = optionalTry.getOrElse(Failure(StepSizeCalculationError("If a stepSize is provided, we also need the startTime & endTime.")))
        innerTry.map { nIntervals =>
          //remove stepSize and add calculated numberOfIntervals
          this.copy(options = (options - "stepSize").updated("numberOfIntervals", nIntervals.asJson))
        }
      } else {
       Success(this)
      }
    }
  }
}
