package webmodelica.core

import webmodelica.models.errors._

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.exceptions._
import com.twitter.finatra.http.response.{ErrorsResponse, ResponseBuilder}
import com.google.inject.{
  Inject,
  Singleton
}

/** A Finatra https://twitter.github.io/finatra/user-guide/http/exceptions.html[ExceptionMapper] for our custom [webmodelica.models.errors]. */
@Singleton
class WMExceptionMapper@Inject()(responseBuilder: ResponseBuilder)
    extends ExceptionMapper[WMException] {
  override def toResponse(request: Request, exception: WMException): Response = {
    //basically a copy of finatras HTTPExceptionMapper but always return json.
    //https://github.com/twitter/finatra/blob/develop/http/src/main/scala/com/twitter/finatra/http/internal/exceptions/HttpExceptionMapper.scala
    responseBuilder
      .status(exception.status)
      .json(ErrorsResponse(Seq(exception.getMessage)))
  }
}
