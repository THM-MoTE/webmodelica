package webmodelica.services

import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim, JwtUtils}
import webmodelica.WMSpec
import webmodelica.models._
import java.time.Instant

import better.files._
import com.twitter.util.Await

import scala.concurrent.duration._

class AuthTokenValidatorSpec extends WMSpec  {
  val privateKey = Resource.getAsString("rsa-key-example")
  val publicKey = Resource.getAsString("rsa-key-example.pub")
  require(privateKey.nonEmpty, "private key can't be empty")
  require(publicKey.nonEmpty, "public key can't be empty")

  val validator = AuthTokenValidator(KeyString(publicKey))

  val iatSeconds = Instant.now().toEpochMilli
  val expSeconds = Instant.now().plusSeconds((1 hour).toSeconds).toEpochMilli

  val payload =
    s"""
      |{
      |  "data": {
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
      |  "exp": $expSeconds,
      |  "iat": $iatSeconds
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
