/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.services

import com.twitter.util.Future
import pdi.jwt.exceptions.{JwtException, JwtValidationException}
import webmodelica.models.User

trait TokenValidator {
  def decode(token:String): Future[UserToken]
  def decodeToUser(token:String): Future[Option[User]]
  def isValid(token:String): Boolean
}

object TokenValidator {
  /** Combines the 2 validators by first running 'a' and if a failes try running 'b'. */
  def combine(a:TokenValidator, b:TokenValidator): TokenValidator = new TokenValidator {
    override def decode(token: String): Future[UserToken] =
      a.decode(token).rescue {
        case _:JwtException => b.decode(token)
      }
    override def decodeToUser(token:String): Future[Option[User]] = {
      a.decodeToUser(token)
        .flatMap {
          case None => b.decodeToUser(token)
          case opt:Some[User] => Future.value(opt)
        }
        .rescue {
          case _:JwtException => b.decodeToUser(token)
        }
    }
    override def isValid(token: String): Boolean = a.isValid(token) || b.isValid(token)
    override def toString: String = s"CombinedValidator($a, $b)"
  }
}
