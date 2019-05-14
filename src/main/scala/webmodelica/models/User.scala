package webmodelica.models

import io.scalaland.chimney.dsl._
import io.circe.generic.JsonCodec

@JsonCodec
case class User(username:String, email: String, first_name: Option[String], last_name: Option[String], hashedPassword:String) {
  def toAuthUser:AuthUser = this.into[AuthUser].transform
}

case class UserDocument(_id: String, email: String, first_name: Option[String], last_name: Option[String], hashedPassword: String)

@JsonCodec
case class AuthUser(username:String, email: String, first_name: Option[String], last_name: Option[String]) {
  def toUser:User = this.into[User].withFieldComputed(_.hashedPassword, _ => "").transform
}

object User {
  def apply(u:UserDocument): User = u.into[User].withFieldRenamed(_._id, _.username).transform
  def apply(username:String, email:String, pw:String): User = User(username, email, None, None, pw)
}

object UserDocument {
  def apply(u:User): UserDocument = u.into[UserDocument].withFieldRenamed(_.username, _._id).transform
}
