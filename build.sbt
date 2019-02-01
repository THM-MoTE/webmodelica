fork in run := true
connectInput in run := true

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Ypartial-unification"
)

resolvers += Resolver.sonatypeRepo("releases")

// initialCommands in console := """""".stripMargin

val finatraVersion = "19.1.0"

lazy val root = (project in file("."))
  .settings(
    //include ./conf in classpath
    unmanagedResourceDirectories in Compile += baseDirectory.value / "conf",
    scalaVersion := "2.12.8",
    name := "webmodelica",
    organization := "de.thm.mote",
    mainClass in Compile := Some("webmodelica.core.WMServerMain"),
    libraryDependencies ++= Seq(
      "com.twitter"   %% "finatra-http"    % finatraVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.github.finagle" %% "finagle-oauth2" % finatraVersion,
      "org.typelevel" %% "cats-core" % "1.6.0",
      "javax.activation" % "activation" % "1.1.1", //java EE package needed for finagle because it's not provided anymore since java 11
    ),
    // dependencyOverrides ++= Seq(
    //   "com.google.guava"         % "guava"  % "19.0"
    // ),
  )