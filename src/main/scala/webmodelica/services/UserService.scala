package webmodelica.services

import webmodelica.stores.UserStore
import webmodelica.models._
import webmodelica.models.errors._

import com.twitter.util.Future
import com.twitter.cache.FutureCache
import com.twitter.finatra.http.exceptions.NotFoundException
import com.google.inject.Inject
import scala.collection.JavaConverters._

class UserService@Inject()(underlying:UserStore)
    extends UserStore
    with com.twitter.inject.Logging {

  info("UserService with caching started...")
  val map = new java.util.concurrent.ConcurrentHashMap[String, Future[User]]()
  val cache = FutureCache.fromMap(
    (k:String) => underlying.findBy(k).flatMap(notFoundExc("user not found!")),
    map
  )

  override def add(u:User): Future[Unit] = underlying add u
  override def findBy(username: String): Future[Option[User]] = {
    cache(username).map(Some.apply)
      .handle {
        case _ =>
          warn(s"didn't find a value for $username")
          None
      }
  }
}
