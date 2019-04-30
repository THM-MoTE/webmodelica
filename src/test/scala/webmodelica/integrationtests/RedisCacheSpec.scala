package webmodelica.integrationtests

import webmodelica.services._
import webmodelica.stores._
import webmodelica.models._
import webmodelica.models.mope._
import webmodelica.models.mope.requests._
import webmodelica.models.mope.responses._
import webmodelica.core.AppModule
import com.twitter.util.{Future,Await}
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

  val config = AppModule.configProvider
  val cache = new RedisCacheImpl[Person](config.redis, "test:stub", underlyingStore)

  "The redis cache service" should "add a value to the cache" in {
    Await.result(cache.update("tim", tim))
  }
  it should "find the added value" in {
    Await.result(cache.find[Person]("tim")) shouldBe Some(tim)
    storeCounter shouldBe 0 //check that no backend was called
  }
  it should "call underlying store if no value available" in {
    Await.result(cache.find[Person]("nico")) shouldBe Some(nico)
    storeCounter shouldBe 1
  }
  it should "not save a value if it's not in the underlying store" in {
    Await.result(cache.find[Person]("blup")) shouldBe None
    storeCounter shouldBe 2
  }
}
