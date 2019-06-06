package webmodelica.services

import java.net.URL

import com.twitter.finagle.Http
import com.twitter.conversions.StorageUnitOps._
import com.twitter.util.StorageUnit

/** a featherbed.Client that increases the maxResponseSize so that we can fetch large datasets - for example: in simulation data - from MoPE. */
class CustomFeatherbedClient(url:URL)
  extends featherbed. Client(url)
  with com.twitter.inject.Logging {

  protected override def clientTransform(client: Http.Client): Http.Client = {
    val responseSize = 500.megabyte
    debug(s"setting maxResponseSize to $responseSize")
    client.withMaxResponseSize(responseSize)
  }
}
