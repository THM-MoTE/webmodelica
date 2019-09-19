package webmodelica.services
import akka.http.javadsl.model.RequestEntity
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

object AkkaHttpClient {
  case class Error(status:StatusCode, reason:String, response:HttpResponse) extends RuntimeException(s"$reason: $response")
}

class AkkaHttpClient(http:HttpExt, baseUri:Uri)
  extends com.typesafe.scalalogging.LazyLogging{

  import http.system.dispatcher
  implicit val mat = ActorMaterializer.create(http.system)

  def unmarshal[E:Decoder](response:HttpResponse): Future[E] = {
    logger.debug(s"decoding $response")
    if(response.status.isSuccess())
      Unmarshal(response).to[E]
    else
      response.entity.dataBytes.runFold(ByteString(""))(_++_)
        .flatMap { byteStr =>
          Future.failed(AkkaHttpClient.Error(response.status, byteStr.utf8String, response))
        }
  }
  def marshal[E:Encoder](entity:E): HttpEntity.Strict = {
    logger.debug(s"encoding $entity")
    val json = implicitly[Encoder[E]].apply(entity)
    HttpEntity(ContentTypes.`application/json`, json.noSpaces)
  }

  def get[E:Decoder](path:Uri): Future[E] = {
    val uri = path.resolvedAgainst(baseUri)
    logger.debug(s"fetching $uri")
    http.singleRequest(HttpRequest(uri=uri))
      .flatMap(unmarshal[E])
  }

  def post[Req:Encoder, Rep:Decoder](path:Uri, entity:Req): Future[Rep] = {
    val uri = path.resolvedAgainst(baseUri)
    logger.debug(s"posting $uri")
    http.singleRequest(HttpRequest(method=HttpMethods.POST, uri=uri, entity = marshal(entity)))
      .flatMap(unmarshal[Rep])
  }
}
