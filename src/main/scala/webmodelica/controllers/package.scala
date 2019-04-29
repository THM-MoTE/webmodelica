package webmodelica

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.util.Future
import webmodelica.models.{User, errors}
import webmodelica.services.{TokenGenerator, UserToken}
import webmodelica.stores.UserStore

package object controllers {
  def extractTokenProvider(tokenGenerator:TokenGenerator)(req:Request): Future[UserToken] = {
    val token = req.headerMap.getOrNull(constants.authorizationHeader)
    tokenGenerator.decode(token)
  }

  def extractUserProvider(userStore:UserStore, tokenGenerator:TokenGenerator)(req:Request): Future[User] = {
    for {
      token <- extractTokenProvider(tokenGenerator)(req)
      user <- userStore.findBy(token.username).flatMap(errors.notFoundExc("web-token contains invalid user informations!"))
    } yield user
  }

  def sendFile(builder:ResponseBuilder)(contentType:String, file:java.io.File): Response = {
    builder.ok
      .header("Content-Disposition", s"""attachment; filename="${file.getName}"""")
      .contentType(contentType)
      .file(file)
  }
}
