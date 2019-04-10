package webmodelica.integrationtests

import org.scalatest._

import webmodelica._
import webmodelica.core._
import webmodelica.models._
import webmodelica.controllers._
import webmodelica.conversions.futures._
import webmodelica.constants

import featherbed._
import featherbed.circe._
import io.circe.syntax._
import io.circe.Json
import io.circe.generic.auto._
import com.twitter.util.{Future,Await}

class ProjectEndpointSpec
    extends AsyncWMSpec {
  val userClient = new featherbed.Client(new java.net.URL(baseUrl+"users/"))
  val client = new featherbed.Client(new java.net.URL(baseUrl+"projects"))

  var token: String = _
  override def beforeAll = {
    super.beforeAll
    token = loginTestUser(userClient).token
  }

  def projectsReq = client.get("projects")
    .withHeader(constants.authorizationHeader, token)
    .accept("application/json")

  "The /projects endpoint" should "create a project" in {
    val req = client.post("projects")
      .withHeader(constants.authorizationHeader, token)
      .withContent(
        Json.obj(
          "owner" -> user.username.asJson,
          "name" -> s"test-project-$uuidStr".asJson),
        "application/json")
      .accept("application/json")

    req.send[JSProject]()
      .handle(catchError)
      .map {proj =>
      proj.id should not be ('empty)
      proj.owner shouldBe (user.username)
    }.asScala
  }
  it should "return all projects" in {
    projectsReq.send[Seq[JSProject]]()
      .map{ seq => seq should not be ('empty) }
      .handle(catchError)
      .asScala
  }
  it should "return a specific project" in {
    projectsReq.send[Seq[JSProject]]()
      .handle(catchError)
      .map(seq => seq.last)
      .flatMap { proj =>
        val req = client.get(s"projects/${proj.id}")
          .withHeader(constants.authorizationHeader, token)
          .accept("application/json")

        req.send[JSProject]()
          .handle(catchError)
          .map { projNew => projNew.id shouldBe(proj.id) }
      }
      .asScala
  }
}
