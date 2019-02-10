package webmodelica.services

import java.nio.file.{
  Path,
  Paths
}

import com.twitter.finagle.Service
import com.twitter.finagle.Http
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finatra.json.FinatraObjectMapper
import java.net.URL

import com.twitter.util.{
  Future,
  Promise
}
import com.twitter.io.Buf
import featherbed._
import scala.reflect.Manifest
import webmodelica.models.mope._
import webmodelica.models.mope.requests._
import webmodelica.models.mope.responses._

trait MopeService {
  this: com.twitter.inject.Logging =>

  def json:FinatraObjectMapper
  def pathMapper: MopeService.PathMapper
  val client: featherbed.Client

  private val projectId: Promise[Int] = Promise[Int]

  private def postJson[O:Manifest](path:String)(in:Any): Future[O] = {
    val str = json.writeValueAsString(in)
    info(s"sending: $in")
    client.post(path)
      .withContent(Buf.Utf8(str), "application/json")
      .send[Response]()
      .map { r => json.parse(r.content) }
  }

  def connect(path:Path):Future[Int] = {
    postJson[Int]("connect")(ProjectDescription(pathMapper.toBindPath(path).toString))
      .map { id =>
        projectId.setValue(id)
        id
      }
      .handle {
        case request.ErrorResponse(req,resp) =>
          val str = s"Error response $resp to request $req"
          throw new Exception(str)
      }
  }

  def compile(path:Path): Future[Seq[CompilerError]] = {
    projectId.flatMap { id =>
    postJson[Seq[CompilerError]](s"project/$id/compile")(FilePath(pathMapper.toBindPath(path).toString))
    }
      .handle {
        case request.ErrorResponse(req,resp) =>
          val str = s"Error response $resp to request $req"
          throw new Exception(str)
      }

  }

  def complete(c:Complete): Future[Seq[Suggestion]] = {
    val cNew = c.copy(file=pathMapper.toBindPath(Paths.get(c.file)).toString)
    projectId.flatMap{ id =>
      postJson(s"project/$id/completion")(cNew)
    }
      .handle {
        case request.ErrorResponse(req,resp) =>
          val str = s"Error response $resp to request $req"
          throw new Exception(str)
      }
object MopeService {
  trait PathMapper {
    def relativize(p:Path): Path
    def toBindPath(p:Path): Path
    def toHostPath(p:Path): Path
    def projectDirectory: Path
  }
  def pathMapper(hostPath:Path, bindPath:Path): PathMapper = new PathMapper() {
    private val stripPath = (from:Path, other:Path) => from.subpath(other.getNameCount, from.getNameCount)
    override def projectDirectory: Path = bindPath
    override def relativize(p:Path): Path =
      if(p.startsWith(hostPath)) stripPath(p, hostPath)
      else stripPath(p, bindPath)
    override def toBindPath(p:Path): Path = {
      if(p.isAbsolute) bindPath.resolve(stripPath(p, hostPath))
      else bindPath.resolve(p)
    }
    override def toHostPath(p:Path): Path =
      if(p.isAbsolute) hostPath.resolve(stripPath(p, bindPath))
      else hostPath.resolve(p)
  }
}
