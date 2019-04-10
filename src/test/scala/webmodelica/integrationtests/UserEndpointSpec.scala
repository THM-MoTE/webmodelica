package webmodelica.integrationtests

import org.scalatest._

import webmodelica._
import webmodelica.core._
import webmodelica.models._
import webmodelica.controllers._
import webmodelica.conversions.futures._
import webmodelica.constants

import featherbed.circe._
import io.circe.generic.auto._
import com.twitter.util.{Future,Await}

class UserEndpointSpec
    extends AsyncWMSpec {

  val client = new featherbed.Client(new java.net.URL(baseUrl+"users/"))

  "The /users endpoint" should "register a user at POST /api/v1/users/register" in {
    val req = client.post("register")
      .withContent(user, "application/json")
      .accept("application/json")

    req.send[TokenResponse]().handle(catchError).map(x => succeed).asScala
  }
  it should "login a user at POST /api/v1/users/login" in {
    val req = client.post("login")
      .withContent(LoginRequest(user.username, user.password), "application/json")
      .accept("application/json")

    req.send[TokenResponse]().handle(catchError).map(t => succeed).asScala
  }
  it should "refresh a token at POST /api/v1/users/refresh" in {
    val loginReq = client.post("login")
      .withContent(LoginRequest(user.username, user.password), "application/json")
      .accept("application/json")
    val refreshReq = { (s:String) => client.post("refresh")
      .withHeader(constants.authorizationHeader, s)
      .withContent(LoginRequest(user.username, user.password), "application/json")
      .accept("application/json")
    }

    (for {
      t <- loginReq.send[TokenResponse]().handle(catchError)
      _ <- Future { Thread.sleep(3000) }
      t2 <- refreshReq(t.token).send[TokenResponse]().handle(catchError)
    } yield (t should not be t2)
    ).asScala
  }
}
