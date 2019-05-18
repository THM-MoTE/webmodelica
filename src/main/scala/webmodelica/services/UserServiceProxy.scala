package webmodelica.services

import com.google.inject.Inject
import com.twitter.finagle.http
import com.twitter.util.Future
import webmodelica.models.{AuthUser, User}
import webmodelica.models.config.UserServiceConf
import webmodelica.stores.UserStore
import featherbed._
import webmodelica.models.errors.UserServiceError

class UserServiceProxy@Inject()(conf:UserServiceConf)
  extends UserStore
    with com.twitter.inject.Logging {
  import featherbed.circe._
  import io.circe.generic.auto._

  val url = new java.net.URL(conf.address+ "/"+ conf.resource+"/")
  def clientProvider() = new featherbed.Client(url)

  info("UserServiceProxy started.")
  info(s"user-service at $url")

  private def withClient[A](fn: featherbed.Client => Future[A]): Future[A] = {
    debug("Using new client")
    val cl = clientProvider()
    fn(cl).ensure {
      debug("releasing client")
      cl.close()
    }
  }

  override def add(u: User): Future[Unit] = {
    debug(s"adding $u")
    withClient { client =>
      val req = client.post("")
        .withContent(u, "application/json")
        .accept("application/json")

      req.send[AuthUser]().map(_ => ())
        .handle {
          case request.ErrorResponse(req, resp) =>
            val str = s"Error in 'add' $resp to request $req"
            throw UserServiceError(str)
        }
    }
  }

  override def findBy(username: String): Future[Option[User]] = {
    debug(s"searching $username")
    withClient { client =>
      val req = client.get(s"$username")
        .accept("application/json")

      req.send[AuthUser]().map(u => Some(u.toUser))
        .handle {
          case request.ErrorResponse(req,resp) if resp.status == http.Status.NotFound => None
          case request.ErrorResponse(req,resp) =>
            val str = s"Error in 'findBy' $resp to request $req"
            throw UserServiceError(str)
        }
    }
  }
}
