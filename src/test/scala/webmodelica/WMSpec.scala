package webmodelica

import org.mongodb.scala.model.Filters
import org.scalatest._
import webmodelica.core.AppModule

import scala.concurrent.Await
import scala.concurrent.duration.Duration

abstract class WMSpec
    extends FlatSpec
    with Matchers
    with Inspectors
    with BeforeAndAfterAll {
  AppModule.env.parse("test")
}

abstract class AsyncWMSpec
    extends AsyncFlatSpec
    with Matchers
    with Inspectors
    with BeforeAndAfterAll {
  def uuidStr: String = java.util.UUID.randomUUID().toString
}

abstract class DBSpec(collectionName:Option[String]=None) extends WMSpec {
  val conf = AppModule.configProvider
  val database = AppModule.mongoDBProvider(conf.mongodb, AppModule.mongoClientProvider(conf.mongodb))

  override def afterAll:Unit = {
    collectionName.foreach { name =>
      Await.result( database.getCollection(name).drop().head(), Duration.Inf )
    }
  }
}
