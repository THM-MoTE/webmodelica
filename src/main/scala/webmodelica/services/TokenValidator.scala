package webmodelica.services

import com.twitter.util.Future
import pdi.jwt.exceptions.{JwtException, JwtValidationException}

trait TokenValidator {
  def decode(token:String): Future[UserToken]
  def isValid(token:String): Boolean
}

object TokenValidator {
  def combine(a:TokenValidator, b:TokenValidator): TokenValidator = new TokenValidator {
    override def decode(token: String): Future[UserToken] =
      a.decode(token).rescue {
        case _:JwtException => b.decode(token)
      }
    override def isValid(token: String): Boolean = a.isValid(token) || b.isValid(token)

    override def toString: String = s"CombinedValidator($a, $b)"
  }
}
