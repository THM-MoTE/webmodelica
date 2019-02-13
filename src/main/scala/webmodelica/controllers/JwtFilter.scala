package webmodelica.controllers

import com.google.inject.Inject
import com.twitter.finagle.{Service, SimpleFilter, http}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.util.Future
import webmodelica.constants
import webmodelica.services.TokenGenerator

class JwtFilter@Inject()(gen:TokenGenerator) extends SimpleFilter[http.Request, http.Response]
  with com.twitter.inject.Logging {

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    request.headerMap.get(constants.authorizationHeader) match {
      case Some(token) if gen.isValid(token) => service(request)
      case _ =>
        warn(s"provided token invalid!")
        val res = Response()
        res.status = Status.Unauthorized
        res.contentString = "Invalid web-token!"
        Future.value(res)
    }
  }
}
