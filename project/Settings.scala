import sbt._
import sbt.Keys._

object Settings {
  def settings = Seq(
    unmanagedResourceDirectories in Compile += baseDirectory.value / "conf",
    organization := "de.thm.mote",
    scalaVersion := "2.12.8",
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-feature",
      "-explaintypes",
      "-Ypartial-unification",
      "-encoding", "utf8",
      "-language:higherKinds",
      "-language:postfixOps",
      "-Xfatal-warnings",
    )
  )

  def latestCommitHash():String = {
    import scala.sys.process._
    ("git rev-parse HEAD" !!).take(8).trim
  }

  def consoleInit:String =
    """import webmodelica._
|import webmodelica.models._, webmodelica.stores._, webmodelica.services._
|import webmodelica.core._, webmodelica.controllers._
|import java.nio.file._
|import better.files._
|import io.circe._, io.circe.syntax._, io.circe.parser._""".stripMargin

}

object Dependencies {
  val finagleVersion = "19.6.0"

  val utils = Seq(
    "com.github.pureconfig" %% "pureconfig" % "0.10.1",
    "org.typelevel" %% "cats-core" % "1.6.0",
    "com.github.pathikrit" %% "better-files" % "3.7.+",
    "io.scalaland" %% "chimney" % "0.3.+",
    "com.pauldijou" %% "jwt-core" % "1.1.+",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.softwaremill.macwire" %% "macros" % "2.3.+",
    "org.rogach" %% "scallop" % "3.3.+",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.+",
    "io.circe" %% "circe-core" % "0.11.+",
    "io.circe" %% "circe-generic" % "0.11.+",
    "com.twitter" %% "util-core" % "19.9.+"
  )

  val akka = Seq(
    "com.typesafe.akka" %% "akka-http"   % "10.1.8",
    "com.typesafe.akka" %% "akka-stream" % "2.5.23",
    "com.typesafe.akka" %% "akka-slf4j" % "2.5.23",
    "de.heikoseeberger" %% "akka-http-circe" % "1.27.0",
  )

  val deps = Seq(
    "com.github.scredis" %% "scredis" % "2.2.6",
    "org.mongodb.scala" %% "mongo-scala-driver" % "2.5.+",
    "org.scalatest" %% "scalatest" % "3.0.+" % "test",
    "org.scalacheck" %% "scalacheck" % "1.14.+" % "test"
  ) ++ utils ++ akka

  val generatorDeps = Seq(
    "com.sksamuel.avro4s" %% "avro4s-core" % "3.0.0-RC2"
  ) ++ utils
}
