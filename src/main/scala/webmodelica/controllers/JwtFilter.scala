package webmodelica.controllers

import com.google.inject.Inject
import com.twitter.finagle.{Service, SimpleFilter, http}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.util.Future
import webmodelica.constants
import webmodelica.services.TokenGenerator
import webmodelica.stores.UserStore
import webmodelica.models.{User, errors}


class JwtFilter@Inject()(gen:TokenGenerator, store:UserStore) extends SimpleFilter[http.Request, http.Response]
  with com.twitter.inject.Logging {

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    request.headerMap.get(constants.authorizationHeader) match {
      case Some(token) if gen.isValid(token) =>
        val userF = gen.decode(token).flatMap(t => store.findBy(t.username))
        val respF = service(request)
        for {
          response <- respF
          user <- userF.flatMap(errors.notFoundExc("web-token contains invalid user informations!"))
          token = gen.newToken(user)
        } yield {
          response.authorization = token
          response
        }
      case _ =>
        warn(s"provided token invalid!")
        val res = Response()
        res.status = Status.Unauthorized
        res.contentString = "Invalid web-token!"
        Future.value(res)
    }
  }
}
