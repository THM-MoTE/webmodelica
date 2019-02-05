package webmodelica.stores

import scala.concurrent.{ ExecutionContext, Future => SFuture }
import webmodelica.models._
import webmodelica.constants
import webmodelica.conversions.futures._
import org.mongodb.scala._
import com.google.inject._
import ExecutionContext.Implicits.global
import com.twitter.util.{Future => TFuture}

class ProjectStore @Inject()(db:MongoDatabase)
    extends DocumentWriters {
  private val collection:MongoCollection[Project] = db.getCollection(constants.projectCollection)

  def add(p:Project): TFuture[Unit] = {
    collection.insertOne(p).head().onComplete { _ =>
      collection.find().toFuture().onComplete(xs => println(s"database content: $xs"))
    }
    //.map(_ => ()).asTwitter
    TFuture.value(())
  }
}
