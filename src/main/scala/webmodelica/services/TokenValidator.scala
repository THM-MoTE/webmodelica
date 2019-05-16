package webmodelica.services

import com.twitter.util.Future

trait TokenValidator {
  def decode(token:String): Future[UserToken]
  def isValid(token:String): Boolean
}
