package webmodelica.services

import java.nio.file.Path

import com.twitter.finagle.Service
import com.twitter.finagle.Http
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finatra.json.FinatraObjectMapper
import java.net.URL

import com.twitter.io.Buf
import featherbed._

case class Connect(path: String, outputDirectory:String="target")

trait MopeService {
  def json:FinatraObjectMapper
  val client: featherbed.Client

  def connect(path:Path) = {
    val con = json.writeValueAsString(Connect(path.toString))
    client.post("connect")
      .withContent(Buf.Utf8(con), "application/json")
      .send[Response]()
      .handle {
        case request.ErrorResponse(req,resp) =>
          throw new Exception(s"Error response $resp to request $req")
      }
  }
}
