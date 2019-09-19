package webmodelica.services
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.unmarshalling.Unmarshal
import webmodelica.models.JsonSupport._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.{Decoder, Encoder}
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.Future

class AkkaHttpClient(http:HttpExt, baseUri:Uri)
  extends com.typesafe.scalalogging.LazyLogging{

  import http.system.dispatcher
  implicit val mat = ActorMaterializer.create(http.system)

  def unmarshal[E:Decoder](response:HttpResponse): Future[E] = {
    logger.debug(s"decoding $response")
    Unmarshal(response).to[E]
  }
  def marshal[E:Encoder](entity:E, request:HttpRequest) = {
    logger.debug(s"encoding $entity")
    Marshal(entity).toResponseFor(request)
  }

  def get[E:Decoder](path:Uri): Future[E] = {
    val uri = path.resolvedAgainst(baseUri)
    logger.debug(s"fetching $uri")
    http.singleRequest(HttpRequest(uri=uri))
      .flatMap(unmarshal[E])
  }
}
