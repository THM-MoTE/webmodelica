package webmodelica.stores

import scala.concurrent.{ ExecutionContext, Future => SFuture }
import webmodelica.models._
import webmodelica.constants
import org.mongodb.scala._
import com.google.inject._
import ExecutionContext.Implicits.global

class ProjectStore @Inject()(db:MongoDatabase)
    extends DocumentWriters {
  private val collection:MongoCollection[Project] = db.getCollection(constants.projectCollection)

  def add(p:Project) = {
    collection.insertOne(p).head().onComplete(x => println(s"writing returned $x"))
    collection.find().head().onComplete{x => println(s"documents: $x") }
  }
}
