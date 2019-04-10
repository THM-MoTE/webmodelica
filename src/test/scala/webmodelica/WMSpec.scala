package webmodelica

import org.mongodb.scala.model.Filters
import org.scalatest._
import webmodelica.core.AppModule
import webmodelica.models._
import webmodelica.controllers._

import scala.concurrent.{Await => SAwait}
import scala.concurrent.duration.Duration
import featherbed._
import featherbed.circe._
import io.circe.generic.auto._

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
  val baseUrl = "http://localhost:8888/api/v1/"
  def uuidStr: String = java.util.UUID.randomUUID().toString

  def catchError[A]: PartialFunction[Throwable, A] = {
    case request.ErrorResponse(req,resp) =>
      val str = s"Error response $resp to request $req : ${resp.contentString}"
      throw new RuntimeException(str)
  }

  val user = RegisterRequest("test-user", "test-user@xample.org", "456789")
  def loginTestUser(client:featherbed.Client):TokenResponse = {
    val req = client.post(s"${baseUrl}users/login")
      .withContent(LoginRequest(user.username, user.password), "application/json")
      .accept("application/json")
    com.twitter.util.Await.result(req.send[TokenResponse]().handle(catchError))
  }
}

abstract class DBSpec(collectionName:Option[String]=None) extends WMSpec {
  val conf = AppModule.configProvider
  val database = AppModule.mongoDBProvider(conf.mongodb, AppModule.mongoClientProvider(conf.mongodb))

  override def afterAll:Unit = {
    collectionName.foreach { name =>
      SAwait.result( database.getCollection(name).drop().head(), Duration.Inf )
    }
  }
}
