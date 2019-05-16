package webmodelica.services

import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim, JwtUtils}
import webmodelica.WMSpec
import webmodelica.core.AppModule
import webmodelica.models._
import java.security._
import java.time.Instant
import java.time.temporal.TemporalUnit
import java.util.concurrent.TimeUnit

import better.files._
import com.twitter.util.Await
import org.joda.time.Hours

import scala.concurrent.duration._

class AuthTokenValidatorSpec extends WMSpec  {
  val privateKey = Resource.getAsString("rsa-key-example")
  val publicKey = Resource.getAsString("rsa-key-example.pub")
  require(privateKey.nonEmpty, "private key can't be empty")
  require(publicKey.nonEmpty, "public key can't be empty")

  val validator = new AuthTokenValidator(KeyString(publicKey))

  val expSeconds = Instant.now().plusSeconds((1 hour).toSeconds).toEpochMilli

  val payload =
    s"""
      |{
      |  "user": {
      |    "id": {
      |      "oid": "abcdferölkjasdf"
      |    },
      |    "username": "test-1",
      |    "first_name": "test-1",
      |    "last_name": "test-2",
      |    "email": "test@xample.org",
      |    "identity": {
      |      "_id": {
      |        "oid": "kdkkalykc"
      |      },
      |      "created_at": "2019-01-01T07:04:37.682Z",
      |      "provider": "developer",
      |      "updated_at": "2019-02-01T07:04:37.682Z",
      |      "username": "test-1"
      |    }
      |  },
      |  "exp": $expSeconds
      |}
    """.stripMargin

  val token = Jwt.encode(JwtClaim(payload), privateKey, JwtAlgorithm.RS256)
  println(s"token is $token")

  "The AuthToken service" should "validate a token" in {
    validator.isValid(token) shouldBe true
  }
  it should "decode a valid token" in {
    val uToken = Await.result(validator.decode(token))
    uToken.username shouldBe "test-1"
    uToken.exp shouldBe expSeconds
  }
}
