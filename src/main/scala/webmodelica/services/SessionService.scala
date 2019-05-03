package webmodelica.services

import webmodelica.models.mope.requests.{Complete, ProjectDescription}
import webmodelica.models.mope.responses.Suggestion
import com.google.inject.Inject
import com.twitter.finagle.Service
import com.twitter.finagle.Http
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.json.FinatraObjectMapper
import com.twitter.util.{Future, Time}
import com.twitter.finagle.stats.StatsReceiver
import webmodelica.models.config.{MopeClientConfig, RedisConfig}
import webmodelica.models.{ModelicaFile, Session}
import webmodelica.stores.{FSStore, FileStore}
import webmodelica.models.errors
import webmodelica.constants
import java.nio.file.{
  Path, Paths
}
import better.files._

import scala.concurrent.{ Future => SFuture, Promise =>  SPromise }
import scala.concurrent.ExecutionContext.Implicits.global

class SessionService @Inject()(
  val mopeConf:MopeClientConfig,
  val session:Session,
  redisConf:RedisConfig,
  statsReceiver:StatsReceiver
  )
  extends FileStore
    with MopeService
  with com.twitter.inject.Logging
  with com.twitter.util.Closable {
  override def clientProvider() = new featherbed.Client(new java.net.URL(mopeConf.address+"mope/"))
  val fsStore = FileStore.fromSession(mopeConf.data.hostDirectory, session)
  val suggestionCache = new RedisCacheImpl[Seq[Suggestion]](redisConf, constants.completionCacheSuffix, _ => Future.value(None), statsReceiver)

  private val projDescr = ProjectDescription(fsStore.rootDir.toString)
  override val pathMapper = MopeService.pathMapper(fsStore.rootDir.toAbsolutePath, mopeConf.data.bindDirectory.resolve(fsStore.rootDir.toAbsolutePath.getFileName()))

  info(s"mapper: $pathMapper")
  info(s"fsStore: $fsStore")
  override def rootDir: Path = fsStore.rootDir
  override def update(file: ModelicaFile): Future[Unit] = fsStore.update(file)
  override def files: Future[List[ModelicaFile]] = fsStore.files
  override def delete(p: Path): Future[Unit] = fsStore.delete(p)
  override def rename(oldPath: Path,newPath: Path):Future[ModelicaFile] = fsStore.rename(oldPath, newPath)
  override def close(deadline:Time):Future[Unit] = disconnect()
  override def packageProjectArchive(name:String): Future[java.io.File] = fsStore.packageProjectArchive(name)
  override def copyTo(destination:Path): Future[Unit] = fsStore.copyTo(destination)

  override def complete(c:Complete): Future[Seq[Suggestion]] = {
    suggestionCache.find(c.word).flatMap {
      case Some(s) => Future.value(s)
      case None =>
        super.complete(c).flatMap(suggestionCache.update(c.word, _))
    }
  }

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
  def locateSimulationCsv(modelName:String): Future[Option[java.io.File]] = Future {
    val outputDir = fsStore.rootDir.resolve(projDescr.outputDirectory)
    File(outputDir).list(f => f.name == s"${modelName}_res.csv").take(1).toSeq.headOption.map(_.toJava)
  }
}
