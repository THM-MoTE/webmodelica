package webmodelica.stores

import better.files._
import better.files.Dsl._
import java.nio.file.{
  Path,
  Paths
}
import webmodelica.WMSpec
import webmodelica.models._
import scala.util.Random
import com.twitter.util.Await

class FSStoreSpec extends WMSpec {

  val textStream: () => String = () => Random.alphanumeric.take(32).mkString(" ")

  val root = File.newTemporaryDirectory()
  val paths = Seq(
    "a/b/c/test.mo",
    "a/b/c/t.mo",
    "a/b/simple.mo",
    "a/s.mo"
  ).map(Paths.get(_))

  val modelicaFiles = paths.map(ModelicaFile(_, textStream()))

  val store = new FSStore(Paths.get(root.toString))

  override def afterAll: Unit = { root.delete() }

  "The FSStore" should "generate all parent directories" in {
    Await.result(store.update(modelicaFiles))
    forAll(paths) { path =>
      val f = File(path)/`..`
      f.isDirectory
    }
  }
  it should "write the files" in {
    forAll(modelicaFiles) { file =>
      val content = (root/file.relativePath.toString).contentAsString
      content == file.content
    }
  }
  it should "update the file" in {
    val file = modelicaFiles.last
    val oldContent = (root/file.relativePath.toString).contentAsString
    val newFile = file.copy(content=textStream())
    Await.result(store.update(newFile))
    newFile.content == (root/file.relativePath.toString).contentAsString
  }
}
