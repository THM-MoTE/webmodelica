package webmodelica.services
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim, JwtHeader, JwtOptions}

import scala.util.{Failure, Success, Try}
import com.twitter.util.Future
import java.nio.file.{Files, Path}

import io.circe.generic.JsonCodec
import webmodelica.constants
import webmodelica.conversions.futures
import webmodelica.models.AuthUser
import io.scalaland.chimney.dsl._

sealed trait PublicKey {
  def key:String
}
case class KeyFile(file:Path) extends PublicKey {
  def key:String = Files.readString(file, constants.encoding)
}
case class KeyString(override val key:String) extends PublicKey

@JsonCodec
case class AuthTokenPayload(user: AuthUser, exp:Long) {
  def toUserToken:UserToken =
    this.into[UserToken]
      .withFieldComputed(_.username, _.user.username)
      .withFieldConst(_.iat, None)
      .transform
}

class AuthTokenValidator(publicKey:PublicKey) {
  import io.circe.parser
  private val secret = publicKey.key
  private val algorithm = Seq(JwtAlgorithm.RS256)

  def isValid(token:String): Boolean = Jwt.isValid(token, secret, algorithm)
  def decode(token:String): Future[UserToken] = {
    val tokenTry = Jwt.decodeAll(token,secret, algorithm).toEither
      .flatMap { case (_, payload, _) => parser.parse(payload) }
      .flatMap(_.as[AuthTokenPayload])
      .map(_.toUserToken)

    futures.eitherToFuture(tokenTry)
  }
}
