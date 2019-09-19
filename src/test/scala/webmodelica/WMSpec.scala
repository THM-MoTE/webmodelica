package webmodelica

import org.scalatest._
import webmodelica.core.WebmodelicaModule

import scala.concurrent.Await
import scala.concurrent.duration.Duration

abstract class WMSpec
    extends FlatSpec
    with Matchers
    with Inspectors
    with BeforeAndAfterAll {
  val module = new WebmodelicaModule {
    override def arguments:Seq[String] = Seq("--environment=test")
  }
  val appConf = module.config
}

abstract class DBSpec(collectionName:Option[String]=None) extends WMSpec {
  val database = module.mongoDB

  override def afterAll:Unit = {
    collectionName.foreach { name =>
      Await.result( database.getCollection(name).drop().head(), Duration.Inf )
    }
  }
}
