package webmodelica.stores

import better.files._
import better.files.Dsl._
import java.nio.file.Path
import webmodelica.models._
import webmodelica.models.mope.requests._
import com.twitter.util.Future

class FSStore(root:Path)
  extends FileStore {
  import webmodelica.constants.encoding

  mkdirs(File(root))

  def rootDir: Path = root
  def update(file:ModelicaFile): Future[Unit] = {
    val fd = File(root.resolve(file.relativePath))
    mkdirs(fd/`..`)
    fd.createIfNotExists()
      .write(file.content)(charset=encoding)
    Future.value(())
  }

  def delete(p:Path): Future[Unit] = Future { File(root.resolve(p)).delete() }
  def rename(oldPath:Path, newPath:Path): Future[ModelicaFile] =
    Future {
      val newFile = root.resolve(newPath)
      File(root.resolve(oldPath)).renameTo(newFile.toString)
      ModelicaFile(root.relativize(newFile), File(newFile).contentAsString)
    }

  override def files: Future[List[ModelicaFile]] = {
    Future.value(
      File(root)
        .glob("**.mo")
        .map(f => ModelicaFile(root.relativize(f.path), f.contentAsString))
        .toList
        .sortBy(_.relativePath)
    )
  }

  override def packageProjectArchive(name:String): Future[java.io.File] = Future {
    import scala.sys.process._
    val outDir = File(rootDir) / ProjectDescription(rootDir.toString).outputDirectory
    //package up all files except:
    // - inside ${root}/out/*
    // - ${root} itself
    // - ${root}/out itself
    // - all .zip archives
    val files = File(rootDir).list(file => file!=outDir && file!=File(rootDir) && !outDir.isParentOf(file) && !file.toString.endsWith(".zip"))
    val zipFile = File(s"/tmp/${name}.zip").zipIn(files)
    zipFile.toJava
  }


  override def toString:String = s"FSStore($root)"
}
