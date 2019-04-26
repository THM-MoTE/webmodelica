import sbt._
import sbt.Keys._

object Common {
  def settings = Seq(
    unmanagedResourceDirectories in Compile += baseDirectory.value / "conf",
    organization := "de.thm.mote",
    scalaVersion := "2.12.8",
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-feature",
      "-Ypartial-unification",
      "-encoding", "utf8",
      "-language:higherKinds",
      "-language:postfixOps",
    )
  )

  def latestCommitHash():String = {
    import scala.sys.process._
    ("git rev-parse HEAD" !!).take(8).trim
  }

  def consoleInit:String =
    """import webmodelica._
|import webmodelica.models._
|import webmodelica.stores._
|import webmodelica.services._
|import webmodelica.core._
|import java.nio.file._
|import better.files._
|import io.circe._""".stripMargin

}

object Dependencies {
  val finatraVersion = "19.1.0"

  val utils = Seq(
    "com.github.pureconfig" %% "pureconfig" % "0.10.1",
    "org.typelevel" %% "cats-core" % "1.6.0",
    "com.github.pathikrit" %% "better-files" % "3.7.+",
    "io.scalaland" %% "chimney" % "0.3.+",
    "com.pauldijou" %% "jwt-core" % "1.1.+",
  )

  val deps = Seq(
    "com.twitter"   %% "finatra-http"    % finatraVersion,
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "javax.activation" % "activation" % "1.1.1", //java EE package needed for finagle because it's not provided anymore since java 11
    "org.mongodb.scala" %% "mongo-scala-driver" % "2.5.+",
    "org.scalatest" %% "scalatest" % "3.0.+" % "test",
    "io.github.finagle" %% "featherbed" % "0.3.+",
  ) ++ utils
}
