package webmodelica.models

import io.scalaland.chimney.dsl._
import io.circe.generic.JsonCodec

@JsonCodec
case class User(username:String,
                email: String,
                hashedPassword:String)

case class UserDocument(_id: String, email: String, hashedPassword: String)

object User {
  def apply(u:UserDocument): User = u.into[User].withFieldRenamed(_._id, _.username).transform
}

object UserDocument {
  def apply(u:User): UserDocument = u.into[UserDocument].withFieldRenamed(_.username, _._id).transform
}
