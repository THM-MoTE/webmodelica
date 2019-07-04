/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.services
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim, JwtHeader, JwtOptions}

import scala.util.{Failure, Success, Try}
import com.twitter.util.Future
import java.nio.file.{Files, Path}
import io.circe.generic.JsonCodec
import webmodelica.constants
import webmodelica.conversions.futures
import webmodelica.models.{
  AuthUser,
  User
}
import io.scalaland.chimney.dsl._

sealed trait PublicKey {
  def key:String
}
case class KeyFile(file:Path) extends PublicKey {
  def key:String = new String(Files.readAllBytes(file), constants.encoding)
}
case class KeyString(override val key:String) extends PublicKey

@JsonCodec
case class AuthTokenPayload(data: AuthUser, iat:Long, exp:Long) {
  def toUser:User = data.toUser
  def toUserToken:UserToken =
    this.into[UserToken]
      .withFieldComputed(_.username, _.data.username)
      .transform
}

/** Validator for JWT tokens from AuthSvc. */
trait AuthTokenValidator extends TokenValidator {
  import io.circe.parser
  def publicKey:PublicKey
  private val secret = publicKey.key
  private val algorithm = Seq(JwtAlgorithm.RS256)

  private def decodeTokenPayload(token:String):Future[AuthTokenPayload] = {
    val tokenTry = Jwt.decodeAll(token,secret, algorithm).toEither
      .flatMap { case (_, payload, _) => parser.parse(payload) }
      .flatMap(_.as[AuthTokenPayload])
    futures.eitherToFuture(tokenTry)
  }

  override def isValid(token:String): Boolean = Jwt.isValid(token, secret, algorithm)
  override def decode(token:String): Future[UserToken] = decodeTokenPayload(token).map(_.toUserToken)
  override def decodeToUser(token:String): Future[Option[User]] = decodeTokenPayload(token).map(a => Some(a.toUser))

  override def toString: String = s"AuthTokenValidator($publicKey)"
}
object AuthTokenValidator {
  def apply(key:PublicKey): AuthTokenValidator = new AuthTokenValidator {
    override def publicKey: PublicKey = key
  }
}
