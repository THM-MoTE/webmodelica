package webmodelica.services

import java.nio.file.Path

import com.twitter.finagle.Service
import com.twitter.finagle.Http
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finatra.json.FinatraObjectMapper
import java.net.URL

import com.twitter.io.Buf
import featherbed.request.ErrorResponse

case class Connect(path: String, outputDirectory:String="target")

trait MopeService {
  def mope: Service[Request,Response]
  def json:FinatraObjectMapper
  def baseUri:String
  private lazy val client = new featherbed.Client(new URL(baseUri+"mope/"))

  def connect(path:Path) = {
    val con = json.writeValueAsString(Connect(path.toString))
    client.post("connect")
      .withContent(Buf.Utf8(con), "application/json")
      .send[Response]()
      .handle {
        case ErrorResponse(req,resp) =>
          throw new Exception(s"Error response $resp to request $req")
      }
  }
}
