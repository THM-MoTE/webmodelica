package webmodelica.stores

import scala.concurrent.{ExecutionContext, Future => SFuture}
import webmodelica.models._
import webmodelica.constants
import webmodelica.conversions.futures._
import org.mongodb.scala._
import com.google.inject._

import ExecutionContext.Implicits.global
import com.twitter.util.{Future => TFuture}
import org.mongodb.scala.bson.BsonObjectId
import org.mongodb.scala.model.Filters

class ProjectStore @Inject()(db:MongoDatabase)
    extends DocumentWriters {
  private val collection:MongoCollection[Project] = db.getCollection(constants.projectCollection)

  def add(p:Project): TFuture[Unit] = collection.insertOne(p).head().map(_ => ()).asTwitter
  def all(): TFuture[Seq[Project]] = collection.find().toFuture().asTwitter
  def findBy(id:BsonObjectId): TFuture[Project] = collection.find(Filters.equal("_id", id)).head().asTwitter
}
