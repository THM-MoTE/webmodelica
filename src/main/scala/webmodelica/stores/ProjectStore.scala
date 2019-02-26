package webmodelica.stores

import scala.concurrent.{ExecutionContext, Future => SFuture}
import webmodelica.models._
import webmodelica.constants
import webmodelica.conversions.futures._
import webmodelica.models.errors.ProjectnameAlreadyInUse
import org.mongodb.scala._
import com.google.inject._

import ExecutionContext.Implicits.global
import com.twitter.util.{Future => TFuture}
import com.mongodb.ErrorCategory
import org.mongodb.scala.bson.BsonObjectId
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.Sorts
import org.mongodb.scala.{DuplicateKeyException, MongoCollection, MongoDatabase, MongoWriteException}

class ProjectStore @Inject()(db:MongoDatabase)
    extends DocumentWriters {
  private val collection:MongoCollection[Project] = db.getCollection(constants.projectCollection)

  def add(p:Project): TFuture[Unit] =
    collection.insertOne(p).head().map(_ => ()).asTwitter
    .handle {
      case e:MongoWriteException if e.getError.getCategory == ErrorCategory.DUPLICATE_KEY =>
        throw ProjectnameAlreadyInUse(p.name)
    }

  def all(): TFuture[Seq[Project]] = collection.find()
    .sort(Sorts.ascending("name"))
    .toFuture()
    .asTwitter
  def findBy(id:BsonObjectId, username:String): TFuture[Option[Project]] =
    collection.find(Filters.and(Filters.equal("_id", id), Filters.equal("owner", username)))
      .headOption()
      .asTwitter
  def byUsername(username:String): TFuture[Seq[Project]] =
    collection.find(Filters.equal("owner", username))
      .sort(Sorts.ascending("name"))
      .toFuture()
      .asTwitter
}
