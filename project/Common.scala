import sbt._
import sbt.Keys._

object Common {
  def settings = Seq(
    unmanagedResourceDirectories in Compile += baseDirectory.value / "conf",
    organization := "de.thm.mote",
    version := "0.1-snapshot",
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
}

object Dependencies {
  val finatraVersion = "19.1.0"

  val deps = Seq(
    "com.twitter"   %% "finatra-http"    % finatraVersion,
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.github.finagle" %% "finagle-oauth2" % finatraVersion,
    "org.typelevel" %% "cats-core" % "1.6.0",
    "javax.activation" % "activation" % "1.1.1", //java EE package needed for finagle because it's not provided anymore since java 11
    "org.mongodb.scala" %% "mongo-scala-driver" % "2.5.0",
  )
}
