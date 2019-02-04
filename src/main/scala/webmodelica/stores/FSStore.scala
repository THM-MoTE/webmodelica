package webmodelica.stores

import better.files._
import better.files.Dsl._
import java.nio.file.Path
import webmodelica.models._
import com.twitter.util.Future

class FSStore(root:Path) {
  import webmodelica.constants.encoding

  def update(file:ModelicaFile): Future[Unit] = {
    val fd = File(root.resolve(file.relativePath))
    mkdirs(fd/`..`)
    fd.createIfNotExists()
      .write(file.content)(charset=encoding)
    Future.value(())
  }
  def update(files:Seq[ModelicaFile]):Future[Unit] = {
    Future.join(files.map(update))
  }
}
