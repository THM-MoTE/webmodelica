/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.services

import java.net.URI

import webmodelica.models.mope.requests.{Complete, ProjectDescription, SimulateRequest}
import webmodelica.models.mope.responses.Suggestion
import com.twitter.util.{Future, Time}
import com.twitter.finagle.stats.StatsReceiver
import webmodelica.models.config.{MopeClientConfig, RedisConfig}
import webmodelica.models.{FileTree, ModelicaFile, ModelicaPath, Session, errors}
import webmodelica.stores.{FSStore, FileStore}
import webmodelica.constants
import java.nio.file.{Path, Paths}

import better.files._
import webmodelica.models.errors.SimulationSetupError

import scala.concurrent.{Future => SFuture, Promise => SPromise}
import scala.concurrent.ExecutionContext.Implicits.global

class SessionService(
  val mopeConf:MopeClientConfig,
  val session:Session,
  redisProvider: RedisCacheFactory,
  override val client: AkkaHttpClient)
  extends FileStore
    with MopeService
  with com.typesafe.scalalogging.LazyLogging
  with com.twitter.util.Closable {
  val fsStore = FileStore.fromSession(mopeConf.data.hostDirectory, session)
  val suggestionCache = redisProvider.get[Seq[Suggestion]](constants.completionCacheSuffix, _ => Future.value(None))

  private val modelToFileMapper = FSStore.findFileFor(fsStore.rootDir)(_)

  private val projDescr = ProjectDescription(fsStore.rootDir.toString)
  override val pathMapper = MopeService.pathMapper(fsStore.rootDir.toAbsolutePath, mopeConf.data.bindDirectory.resolve(fsStore.rootDir.toAbsolutePath.getFileName))

  logger.info(s"mapper: $pathMapper")
  logger.info(s"fsStore: $fsStore")
  override def rootDir: Path = fsStore.rootDir
  override def update(file: ModelicaFile): Future[Unit] = fsStore.update(file)
  override def files: Future[List[ModelicaPath]] = fsStore.files
  override def delete(p: Path): Future[Unit] = fsStore.delete(p)
  override def rename(oldPath: Path,newPath: Path):Future[ModelicaPath] = fsStore.rename(oldPath, newPath)
  override def close(deadline:Time):Future[Unit] = disconnect()
  override def packageProjectArchive(name:String): Future[java.io.File] = fsStore.packageProjectArchive(name)
  override def copyTo(destination:Path): Future[Unit] = fsStore.copyTo(destination)
  override def fileTree(projectName:Option[String]=None): Future[FileTree] = fsStore.fileTree(projectName)
  override def findByPath(p:Path): Future[Option[ModelicaFile]] = fsStore.findByPath(p)


  override def simulate(simParam:SimulateRequest): Future[URI] = {
    modelToFileMapper(simParam.modelName).flatMap {
      case Some(path) =>
        compile(path).flatMap { errors =>
          if(errors.isEmpty) {
            logger.info(s"found source file for ${simParam.modelName} at ${path}")
            super.simulate(simParam)
          }
          else
            Future.exception(SimulationSetupError(s"the model ${simParam.modelName} contains compiler errors!"))
        }
      case None =>
        logger.warn(s"don't know where ${simParam.modelName} is stored.. lets hope its compiled already..")
        super.simulate(simParam)
    }
  }

  override def complete(c:Complete): Future[Seq[Suggestion]] = {
    suggestionCache.find(c.word).flatMap {
      case Some(s) =>Future.value(s)
      case None =>
        super.complete(c).flatMap(suggestionCache.update(c.word, _))
    }
  }

  def extractArchive(path:Path): Future[List[ModelicaPath]] = {
    import scala.sys.process._
    Future {
      logger.info(s"extracting $path to ${fsStore.rootDir}")
      Seq("unzip", path.toAbsolutePath.toString, "-d", fsStore.rootDir.toAbsolutePath.toString).!
    }.flatMap {
      case status:Int if status==0 => this.files
      case _ => Future.exception(errors.ArchiveError(s"Unzipping $path failed!"))
    }
  }
  def locateSimulationCsv(modelName:String): Future[Option[java.io.File]] = Future {
    val outputDir = fsStore.rootDir.resolve(projDescr.outputDirectory)
    File(outputDir).list(f => f.name == s"${modelName}_res.csv").take(1).toSeq.headOption.map(_.toJava)
  }
}
