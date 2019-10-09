/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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
import akka.http.scaladsl.model.headers.{
  Accept,
  RawHeader
}
import akka.stream.ActorMaterializer
import akka.util.ByteString
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.Future

object AkkaHttpClient {
  case class Error(status:StatusCode, reason:String, response:HttpResponse) extends RuntimeException(s"$reason: $response")
}

class AkkaHttpClient(http:HttpExt, baseUri:Uri, maxPayloadSize:Option[Int]=None)
  extends com.typesafe.scalalogging.LazyLogging {

  logger.info(s"client for $baseUri initialized with payload: $maxPayloadSize.")

  import http.system.dispatcher
  implicit val mat = ActorMaterializer.create(http.system)

  def addHeaders(headers: Seq[(String,String)]):List[HttpHeader] = {
    List(Accept(MediaTypes.`application/json`)) ++ headers.map { case (n,v) => new RawHeader(n,v) }
  }

  def unmarshal[E:Decoder](response:HttpResponse): Future[E] = {
    logger.debug(s"decoding $response")
    val entity = maxPayloadSize.map(size => response.entity.withSizeLimit(size)).getOrElse(response.entity)
    if(response.status.isSuccess())
      Unmarshal(entity).to[E]
    else
      entity.dataBytes.runFold(ByteString(""))(_++_)
        .flatMap { byteStr =>
          Future.failed(AkkaHttpClient.Error(response.status, byteStr.utf8String, response))
        }
  }
  def marshal[E:Encoder](entity:E): HttpEntity.Strict = {
    logger.debug(s"encoding $entity")
    val json = implicitly[Encoder[E]].apply(entity)
    HttpEntity(ContentTypes.`application/json`, json.noSpaces)
  }

  def get(path:Uri, headers:Seq[(String,String)]=Seq.empty): Future[HttpResponse] = {
    val uri = path.resolvedAgainst(baseUri)
    logger.debug(s"fetching $uri")
    http.singleRequest(HttpRequest(uri=uri, headers = addHeaders(headers)))
  }

  def post[Req:Encoder](path:Uri, entity:Req, headers:Seq[(String,String)]=Seq.empty): Future[HttpResponse] = {
    val uri = path.resolvedAgainst(baseUri)
    logger.debug(s"posting $uri")
    http.singleRequest(HttpRequest(method=HttpMethods.POST, uri=uri, headers = addHeaders(headers), entity = marshal(entity)))
  }

  def postJson[Req:Encoder, Rep:Decoder](path:Uri, entity:Req, headers:Seq[(String,String)]=Seq.empty): Future[Rep] =
    post(path, entity, headers).flatMap(unmarshal[Rep])
  def getJson[E:Decoder](path:Uri, headers:Seq[(String,String)]=Seq.empty): Future[E] =
    get(path, headers).flatMap(unmarshal[E])
}
