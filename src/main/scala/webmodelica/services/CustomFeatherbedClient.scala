package webmodelica.services

import java.net.URL

import com.twitter.finagle.Http
import com.twitter.conversions.StorageUnitOps._
import com.twitter.util.StorageUnit

/** a featherbed.Client that increases the maxResponseSize so that we can fetch large datasets - for example: in simulation data - from MoPE. */
class CustomFeatherbedClient(url:URL, maxResponseSize:Int = 5)
  extends featherbed. Client(url)
  with com.twitter.inject.Logging {

  protected override def clientTransform(client: Http.Client): Http.Client = {
    //this function is called inside of the constructor of featherbed.Client
    //so we can't use any values outside of this function except constructor arguments of THIS class
    val responseSize = maxResponseSize.megabytes
    info(s"MoPE client responseSize: $responseSize")
    client.withMaxResponseSize(responseSize)
  }
}
