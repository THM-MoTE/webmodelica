package webmodelica.stores

import scala.concurrent.{ ExecutionContext, Future => SFuture }
import reactivemongo.api.{ Cursor, DefaultDB, MongoConnection, MongoDriver }
import webmodelica.models._
import webmodelica.constants

class ProjectStore(db:DefaultDB)
  (implicit context:ExecutionContext)
    extends DocumentWriters {
  private val collection = db.collection(constants.projectCollection)

  def add(p:Project): SFuture[Unit] = {
    collection.insert(false).one(p).map(_ => ())
  }
}
