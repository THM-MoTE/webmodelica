package webmodelica.services

import java.nio.file.Path

import com.twitter.finagle.Service
import com.twitter.finagle.Http
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finatra.json.FinatraObjectMapper
import java.net.URL

import com.twitter.util.Future
import com.twitter.io.Buf
import featherbed._
import scala.reflect.Manifest

case class Connect(path: String, outputDirectory:String="target")

trait MopeService {
  this: com.twitter.inject.Logging =>

  trait PathMapper {
    def relativize(p:Path): Path
    def toBindPath(p:Path): Path
    def toHostPath(p:Path): Path
  }

  def json:FinatraObjectMapper
  def pathMapper:PathMapper
  val client: featherbed.Client

  private def postJson[O:Manifest](path:String)(in:Any): Future[O] = {
    val str = json.writeValueAsString(in)
    info(s"sending: $in")
    client.post(path)
      .withContent(Buf.Utf8(str), "application/json")
      .send[Response]()
      .map { r => json.parse(r.content) }
  }

  def connect(path:Path) = {
    postJson[Int]("connect")(Connect(path.toString))
      .handle {
        case request.ErrorResponse(req,resp) =>
          throw new Exception(s"Error response $resp to request $req")
      }
  }
}
