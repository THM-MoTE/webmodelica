package webmodelica.services

import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim, JwtHeader, JwtOptions}
import webmodelica.models.User

import scala.concurrent.duration._
import scala.util.Try
import java.time.Instant

case class UserTokenPayload(username: String)
case class UserToken(username:String, iat: Long, exp: Long)

class TokenGenerator(secret:String) {
  import io.circe.generic.auto._
  import io.circe.parser
  import io.circe.syntax._

  private val algorithm = JwtAlgorithm.HS384
  private val expiration = (15 minutes).toSeconds

  def newToken(u:User): String = {
    val payload = UserTokenPayload(u.username).asJson.toString
    val claim = JwtClaim(payload).issuedNow.expiresIn(expiration)
    Jwt.encode(claim, secret, algorithm)
  }
  def decode(token:String): Try[UserToken] = {
    Jwt.decodeAll(token, secret, Seq(algorithm)).flatMap {
      case (_, payload,_) => parser.decode[UserToken](payload).toTry
    }
  }
  def isValid(token:String): Boolean = Jwt.isValid(token, secret, Seq(algorithm))
  def validate(token:String): Unit = Jwt.validate(token, secret, Seq(algorithm))
}
