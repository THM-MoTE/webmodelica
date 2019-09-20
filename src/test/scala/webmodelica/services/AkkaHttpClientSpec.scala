/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.services

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri
import io.circe.Json
import io.circe.syntax._
import io.circe.generic.JsonCodec
import org.scalatest._
import webmodelica.WMSpec
import webmodelica.models._

import scala.concurrent.Await
import scala.concurrent.duration._

object AkkaHttpClientSpec {
  @JsonCodec
  case class EchoResponse(args:Map[String,String], headers:Map[String,String], data:Option[Map[String,String]], url:String)

  val bodyFields = Seq(
    "example" -> "test",
    "akka" -> "http"
  )
  def postBody: Json = {
    val fields = bodyFields.map { case (k, v) => k -> v.asJson }
    Json.obj(fields:_*)
  }
}

class AkkaHttpClientSpec
  extends WMSpec {
  import module.actorSystem
  //postman-echo is a service for testing http clients
  //see: https://docs.postman-echo.com
  val baseUri = Uri("https://postman-echo.com")
  val client = new AkkaHttpClient(Http(), baseUri)
  "The akka-based HttpClient" should "fetch json bodies" in {
    val resp = Await.result(client.getJson[AkkaHttpClientSpec.EchoResponse](Uri("get")), 20.seconds)
    resp.url shouldBe baseUri.withPath(Uri.Path("/get")).toString
  }
  it should "post json bodies" in {
    val resp = Await.result(client.postJson[Json, AkkaHttpClientSpec.EchoResponse](Uri("post"), AkkaHttpClientSpec.postBody), 20.seconds)
    resp.data shouldBe a [Some[_]]
    resp.data.get shouldBe AkkaHttpClientSpec.bodyFields.toMap
  }
}
