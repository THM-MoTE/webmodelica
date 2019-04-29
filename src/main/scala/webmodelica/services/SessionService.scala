package webmodelica.services

import webmodelica.models.mope.requests.ProjectDescription
import com.google.inject.Inject
import com.twitter.finagle.Service
import com.twitter.finagle.Http
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.json.FinatraObjectMapper
import com.twitter.util.{Future, Time}
import webmodelica.models.config.MopeClientConfig
import webmodelica.models.{ModelicaFile, Session}
import webmodelica.stores.{FSStore, FileStore}
import webmodelica.models.errors
import java.nio.file.{
  Path, Paths
}
import better.files._

import scala.concurrent.{ Future => SFuture, Promise =>  SPromise }
import scala.concurrent.ExecutionContext.Implicits.global

class SessionService @Inject()(
  val conf:MopeClientConfig,
  val session:Session)
  extends FileStore
    with MopeService
  with com.twitter.inject.Logging
  with com.twitter.util.Closable {
  override def clientProvider() = new featherbed.Client(new java.net.URL(conf.address+"mope/"))
  val fsStore = new FSStore(conf.data.hostDirectory.resolve(session.basePath))
  private val projDescr = ProjectDescription(fsStore.rootDir.toString)
  override val pathMapper = MopeService.pathMapper(fsStore.rootDir.toAbsolutePath, conf.data.bindDirectory.resolve(fsStore.rootDir.toAbsolutePath.getFileName()))

  info(s"mapper: $pathMapper")
  info(s"fsStore: $fsStore")
  override def rootDir: Path = fsStore.rootDir
  override def update(file: ModelicaFile): Future[Unit] = fsStore.update(file)
  override def files: Future[List[ModelicaFile]] = fsStore.files
  override def delete(p: Path): Future[Unit] = fsStore.delete(p)
  override def rename(oldPath: Path,newPath: Path):Future[ModelicaFile] = fsStore.rename(oldPath, newPath)
  override def close(deadline:Time):Future[Unit] = disconnect()

  def extractArchive(path:Path): Future[List[ModelicaFile]] = {
    import scala.sys.process._
    Future {
      info(s"extracting $path to ${fsStore.rootDir}")
      Seq("unzip", path.toAbsolutePath.toString, "-d", fsStore.rootDir.toAbsolutePath.toString).!
    }.flatMap {
      case status:Int if status==0 => this.files
      case _ => Future.exception(errors.ArchiveError("Unzipping $path failed!"))
    }
  }

  def packageProjectArchive(): Future[java.io.File] = Future {
    val directoryContent = File(fsStore.rootDir).list(f => f.name != projDescr.outputDirectory)
    val zipFile = File(s"/tmp/${session.project.name}.zip").zipIn(directoryContent)
    zipFile.toJava
  }

  def locateSimulationCsv(modelName:String): Future[Option[java.io.File]] = Future {
    val outputDir = fsStore.rootDir.resolve(projDescr.outputDirectory)
    File(outputDir).list(f => f.name == s"${modelName}_res.csv").take(1).toSeq.headOption.map(_.toJava)
  }
}
