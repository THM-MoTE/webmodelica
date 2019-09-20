/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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
  //the tree represenation of 'paths'
  val projectTree = Node(
    Paths.get("awesomeProject"),
    List(
      Node("a".asPath,List(
        Node(Paths.get("b"),
          List(
            Node("c".asPath,
              List(
                Leaf("t.mo".asPath, "a/b/c/t.mo".asModelicaPath),
                Leaf("test.mo".asPath, "a/b/c/test.mo".asModelicaPath),
              )
            ),
            Leaf("simple.mo".asPath, "a/b/simple.mo".asModelicaPath)
          )
        ),
        Leaf("s.mo".asPath, "a/s.mo".asModelicaPath)
      ))
    )
  )

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
  it should "create a zip archive containing the files" in {
    //NOTE: this test must be run BEFORE renaming 1 file!
    val archive = File(Await.result(store.packageProjectArchive("anAwesomeArchive")).toPath)
    archive.name shouldBe "anAwesomeArchive.zip"
    val fileNames = archive.newZipInputStream.mapEntries(_.getName).toSet
    forAll(paths) { path =>
      //at least 1 ZipEntry must contain the current path
      //ZipEntries start with a random number as root directory, so we can't check for equality here
      fileNames.exists(s => s.endsWith(path.toString)) shouldBe (true)
    }
  }
  it should "generate a tree from the directory" in {
    //NOTE: this test must be run BEFORE renaming 1 file!
    val storeTree = Await.result(store.fileTree(Some("awesomeProject")))
    storeTree shouldBe (projectTree)
  }
  it should "rename a file" in {
    val oldFile = modelicaFiles.head
    val newPath = Paths.get("a/b/c/test-new.mo")
    Await.result(for {
       _ <- store.update(oldFile)
      newFile <- store.rename(oldFile.relativePath,  newPath)
    } yield newFile.relativePath == newPath)
  }

  it should "delete a file" in {
    val oldFile = modelicaFiles.head
    Await.result(for {
      _ <- store.update(oldFile)
      _ <- store.delete(oldFile.relativePath)
    } yield (root/oldFile.relativePath.toString).notExists shouldBe true)
  }
  it should "find a file by relative path" in {
    val path = Paths.get("a/s.mo")
    val option = Await.result(store.findByPath(path))
    option shouldBe a [Some[_]]
    option.get.relativePath shouldBe (path)
  }
  it should "copy a project into another directory" in {
    val originFiles = store.files
    File.usingTemporaryDirectory() { dir =>
      Await.result(store.copyTo(dir.path))
      val store2 = new FSStore(dir.path)
      val targetPaths = Await.result(store2.files).map(_.relativePath).toSet
      val originPaths = Await.result(originFiles).map(_.relativePath).toSet
      //both path's are relative, so they should be equal
      targetPaths shouldBe (originPaths)
    }
  }
}
