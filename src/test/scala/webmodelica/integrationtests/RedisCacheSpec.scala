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

  val asyncCache = module.redisCacheFactory.get[Person]("test:stub", underlyingStore)

  "The redis cache service" should "add a value to the cache" in {
    Await.result(asyncCache.update("tim", tim))
  }
  it should "find the added value" in {
    Await.result(asyncCache.find("tim")) shouldBe Some(tim)
    storeCounter shouldBe 0 //check that no backend was called
  }
  it should "call underlying store if no value available" in {
    Await.result(asyncCache.find("nico")) shouldBe Some(nico)
    storeCounter shouldBe 1
  }
  it should "not save a value if it's not in the underlying store" in {
    Await.result(asyncCache.find("blup")) shouldBe None
    storeCounter shouldBe 2
  }
  it should "remove a value" in {
    Await.result(asyncCache.update("tim", tim))
    Await.result(asyncCache.remove("tim"))
    Await.result(asyncCache.find("tim")) shouldBe None
  }
}
