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
import scala.concurrent.{ Future => SFuture, Promise =>  SPromise }
import scala.concurrent.ExecutionContext.Implicits.global

import com.twitter.io.Buf
import featherbed._
import scala.reflect.Manifest
import webmodelica.models.mope._
import webmodelica.models.mope.requests._
import webmodelica.models.mope.responses._
import webmodelica.conversions.futures._

trait MopeService {
  this: com.twitter.inject.Logging =>

  def json:FinatraObjectMapper
  def pathMapper: MopeService.PathMapper
  val client: featherbed.Client

  private lazy val projIdPromise: Promise[Int] = new Promise[Int]()
  private def projectId: Future[Int] = projIdPromise

  private def postJson[O:Manifest, I](path:String)(in:I): Future[O] = {
    val str = json.writeValueAsString(in)
    println(s"calling: $path with $str")
    client.post(path)
      .withContent(Buf.Utf8(str), "application/json")
      .send[Response]()
      .map { r => json.parse(r.content) }
      .handle {
        case request.ErrorResponse(req,resp) =>
          val str = s"Error response $resp to request $req"
          throw new Exception(str)
        case e:Exception =>
          error(s"error while connecting ${e.getMessage}")
          throw e
      }
  }


  import featherbed.circe._
  import io.circe.generic.auto._

  def connect():Future[Int] = {
    val path = pathMapper.projectDirectory
    val descr = ProjectDescription(path.toString)
    info(s"connecting with $descr")
    val req = client.post("connect")
      .withContent(descr, "application/json")
      .accept("application/json")

    req.send[Int]().map { id =>
        info(s"registered id $id")
        projIdPromise setValue id
        id
    }
      .handle {
        case request.ErrorResponse(req,resp) =>
          val str = s"Error response $resp to request $req"
          throw new Exception(str)
        case e:Exception =>
          error(s"error while connecting ${e.getMessage}")
          throw e
      }
  }

  def compile(path:Path): Future[Seq[CompilerError]] = {
    val fp = FilePath(pathMapper.toBindPath(path).toString)
    projectId.flatMap { id =>
      info(s"compiling $fp")
      val req = client.post(s"project/$id/compile")
        .withContent(fp, "application/json")
        .accept("application/json")
      req.send[Seq[CompilerError]]()
      .handle {
        case request.ErrorResponse(req,resp) =>
          val str = s"Error response $resp to request $req"
          throw new Exception(str)
      }
    }
  }

  def complete(c:Complete): Future[Seq[Suggestion]] = {
    val cNew = c.copy(file=pathMapper.toBindPath(Paths.get(c.file)).toString)
    projectId.flatMap { id =>
      info(s"complete $cNew")
      val req = client.post(s"project/$id/completion")
        .withContent(cNew, "application/json")
        .accept("application/json")
      req.send[Seq[Suggestion]]()
      .handle {
        case request.ErrorResponse(req,resp) =>
          val str = s"Error response $resp to request $req"
          throw new Exception(str)
      }
    }
  }
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
