/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.stores

import scala.concurrent.{ExecutionContext, Future => SFuture}
import webmodelica.models._
import webmodelica.constants
import webmodelica.conversions.futures._
import webmodelica.models.errors.ProjectnameAlreadyInUse
import org.mongodb.scala._
import com.google.inject._
import com.google.inject.internal.BytecodeGen.Visibility

import ExecutionContext.Implicits.global
import com.twitter.util.{Future => TFuture}
import com.mongodb.ErrorCategory
import org.mongodb.scala.bson.BsonObjectId
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.Sorts
import org.mongodb.scala.model.Updates
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

  def update(p:Project): TFuture[Project] = {
    val replacement = Document("owner" -> p.owner, "name" -> p.name, "visibility" -> p.visibility)
    val updateDoc = Document("$set" -> replacement)
    collection.updateOne(Filters.eq("_id", p._id), updateDoc)
      .toFuture()
      .map(_ => p)
      .asTwitter
  }

  def setVisiblity(pId:String, visibility: String): TFuture[Project] = {
    if(Project isAvailableVisibility visibility) {
      collection.updateOne(Filters.eq("_id", pId), Updates.set("visibility", visibility))
        .head()
        .asTwitter
        .flatMap(_ => find(pId))
        .flatMap(errors.notFoundExc(s"there is no project with id: $pId"))
    } else {
      TFuture.exception(new IllegalArgumentException(s"$visibility is no available visibility! visibilities are: ${Project.visibilities.mkString(",")}"))
    }
  }

  def all(): TFuture[Seq[Project]] = collection.find()
    .sort(Sorts.ascending("name"))
    .toFuture()
    .asTwitter

  def find(id:String): TFuture[Option[Project]] = {
    collection.find(Filters.equal("_id", id))
      .headOption()
      .asTwitter
  }

  def findBy(id:String, username:String): TFuture[Option[Project]] =
    collection.find(Filters.and(Filters.equal("_id", id),
      Filters.or(Filters.equal("owner", username),
      Filters.equal("visibility", Project.publicVisibility))))
      .headOption()
      .asTwitter

  def byUsername(username:String): TFuture[Seq[Project]] =
    collection.find(Filters.or(
      Filters.equal("owner", username),
      Filters.equal("visibility", Project.publicVisibility)))
      .sort(Sorts.ascending("name"))
      .toFuture()
      .asTwitter

  def publicProjects(): TFuture[Seq[Project]] =
    collection.find(Filters.equal("visibility", Project.publicVisibility))
      .sort(Sorts.ascending("name"))
      .toFuture()
      .asTwitter

  def delete(id:String): TFuture[Unit] = {
    collection.deleteOne(Filters.equal("_id", id))
      .head()
      .map(_ => ())
      .asTwitter
  }
}
