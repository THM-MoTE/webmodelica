package webmodelica.integrationtests

import org.scalatest._

import webmodelica._
import webmodelica.core._
import webmodelica.models._
import webmodelica.controllers._
import webmodelica.conversions.futures._
import webmodelica.constants

import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.Json
import io.circe.generic.auto._
import featherbed._
import featherbed.circe._
import java.nio.file.{Paths, Path}
import com.twitter.util.{Future,Await}
import com.twitter.finagle.http.Response

class SessionEndpointSpec
  extends AsyncWMSpec {
  val projectClient = new featherbed.Client(new java.net.URL(baseUrl+"projects"))
  val client = new featherbed.Client(new java.net.URL(baseUrl+"sessions/"))

  var token: String = _
  var project: JSProject = _
  var session: JSSession = _
  override def beforeAll = {
    super.beforeAll
    token = loginTestUser(client).token
  }

  "The /project endpoint" should "create a session at POST /api/v1/projects/:projectId/sessions/new" in {
    //first create a project, then create the session
    val req = projectClient.post("projects")
      .withHeader(constants.authorizationHeader, token)
      .withContent(
        Json.obj(
          "owner" -> user.username.asJson,
          "name" -> s"test-project-$uuidStr".asJson),
        "application/json")
      .accept("application/json")

    project = Await.result(req.send[JSProject]().handle(catchError))
    project.id should not be ('empty)

    val req2 = projectClient.post(s"projects/${project.id}/sessions/new")
        .withHeader(constants.authorizationHeader, token)
        .withContent(0, "application/json")
      .accept("application/json")


    session = Await.result(req2.send[JSSession]().handle(catchError))
    session.id should not be ('empty)
  }
  "The /session endpoint" should "create a file at POST /api/v1/sessions/:sessionId/files/update" in {
    val file = ModelicaFile(Paths.get("a/b/simple.mo"), "model simple end simple;")
    val req2 = client.post(s"${session.id}/files/update")
        .withHeader(constants.authorizationHeader, token)
        .withContent(file, "application/json")
        .accept("application/json")

    req2.send[ModelicaFile]().handle(catchError).map { newFile =>
      newFile shouldBe (file)
    }.asScala
  }
  it should "update a file at POST /api/v1/sessions/:sessionId/files/update" in {
    val file = ModelicaFile(Paths.get("a/b/simple.mo"), "model simp end simple;")
    val req2 = client.post(s"${session.id}/files/update")
        .withHeader(constants.authorizationHeader, token)
        .withContent(file, "application/json")
        .accept("application/json")

    req2.send[ModelicaFile]().handle(catchError).map { newFile =>
      newFile shouldBe (file)
    }.asScala
  }
}
