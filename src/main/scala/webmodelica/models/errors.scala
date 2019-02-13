package webmodelica.models

import com.google.common.net.MediaType
import com.twitter.finatra.http.exceptions.NotFoundException
import com.twitter.util.Future

object errors {
  def notFoundExc[A](reason:String)(opt: Option[A]): Future[A] = opt match {
    case Some(a) => Future.value(a)
    case _ => Future.exception(NotFoundException(reason))
  }

  case class UsernameAlreadyInUse(name:String) extends RuntimeException {
    override def getMessage: String = s"Username `$name` already assigned"
  }

  case object CredentialsError extends RuntimeException {
    override def getMessage: String = "Wrong username or password!"
  }
}
