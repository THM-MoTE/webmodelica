/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.models

import io.scalaland.chimney.dsl._
import io.circe.generic.JsonCodec

@JsonCodec
case class User(username:String, email: String, first_name: Option[String], last_name: Option[String], hashedPassword:String) {
  def toAuthUser:AuthUser = this.into[AuthUser].transform
}

case class UserDocument(_id: String, email: String, first_name: Option[String], last_name: Option[String], hashedPassword: String)

@JsonCodec
case class AuthUser(username:String, email: Option[String], first_name: Option[String], last_name: Option[String]) {
  def toUser:User = this.into[User].withFieldComputed(_.email, au => au.email.getOrElse(au.username+"@webmodelica.me")).withFieldConst(_.hashedPassword, "").transform
}

object User {
  def apply(u:UserDocument): User = u.into[User].withFieldRenamed(_._id, _.username).transform
  def apply(username:String, email:String, pw:String): User = User(username, email, None, None, pw)
}

object UserDocument {
  def apply(u:User): UserDocument = u.into[UserDocument].withFieldRenamed(_.username, _._id).transform
}
