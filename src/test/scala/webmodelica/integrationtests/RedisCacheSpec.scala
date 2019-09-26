/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.integrationtests

import webmodelica.services._
import webmodelica.stores._
import webmodelica.models._
import webmodelica.models.mope._
import webmodelica.models.mope.requests._
import webmodelica.models.mope.responses._
import com.twitter.util.{Future,Await}
import com.twitter.finagle.stats.NullStatsReceiver
import io.circe.generic.JsonCodec
import io.circe.generic.semiauto._

@JsonCodec
case class Person(name:String, age:Int)

class RedisCacheSpec
  extends webmodelica.WMSpec {

  implicit val encoder = deriveEncoder[Person]
  implicit val decoder = deriveDecoder[Person]

  import module._
  import AsyncRedisCache._

  val tim = Person("tim", 16)
  val nico = Person("nico", 19)

  var storeCounter:Int = 0

  val underlyingStore: String => Future[Option[Person]] = (k:String) => {
    storeCounter += 1
    if(k == "nico")
      Future.value(Some(nico))
    else
      Future.value(None)
  }

  val finagleCache = new RedisCacheImpl[Person](appConf.redis, "test:stub", underlyingStore, new NullStatsReceiver())
  val akkaCache = new AsyncRedisCache[Person](scredis.Client(appConf.redis.host,appConf.redis.port), appConf.redis.defaultTtl, "test:stub", underlyingStore)

  "The redis cache service" should "add a value to the cache" in {
    Await.result(finagleCache.update("tim", tim))
    Await.result(akkaCache.update("tim", tim))
  }
  it should "find the added value" in {
    Await.result(finagleCache.find("tim")) shouldBe Some(tim)
    Await.result(akkaCache.find("tim")) shouldBe Some(tim)
    storeCounter shouldBe 0 //check that no backend was called
  }
  it should "call underlying store if no value available" in {
    Await.result(finagleCache.find("nico")) shouldBe Some(nico)
    Await.result(akkaCache.find("nico")) shouldBe Some(nico)
    storeCounter shouldBe 2
  }
  it should "not save a value if it's not in the underlying store" in {
    Await.result(finagleCache.find("blup")) shouldBe None
    Await.result(akkaCache.find("blup")) shouldBe None
    storeCounter shouldBe 4
  }
}
