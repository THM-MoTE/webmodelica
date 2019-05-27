/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.stores

import java.security.MessageDigest

import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import com.google.inject.Inject
import com.mongodb.ErrorCategory
import com.twitter.util.Future
import org.mongodb.scala.model.Filters
import org.mongodb.scala.{DuplicateKeyException, MongoCollection, MongoDatabase, MongoWriteException}
import webmodelica.constants
import webmodelica.models.{DocumentWriters, Project, User, UserDocument}
import webmodelica.conversions.futures._
import webmodelica.models.errors.UsernameAlreadyInUse

trait UserStore {
  def add(u:User): Future[Unit]
  def findBy(username: String): Future[Option[User]]
}

class UserStoreImpl @Inject()(db:MongoDatabase)
    extends UserStore
    with DocumentWriters {
  private val collection:MongoCollection[UserDocument] = db.getCollection(constants.userCollection)

  def add(u:User): Future[Unit] =
    collection.insertOne(UserDocument(u)).head().map(_ => ()).asTwitter
    .handle {
      case e:MongoWriteException if e.getError.getCategory == ErrorCategory.DUPLICATE_KEY =>
        throw UsernameAlreadyInUse(u.username)
    }
  def findBy(username: String): Future[Option[User]] =
    collection.find(Filters.equal("_id", username))
      .headOption()
      .map(_.map(User.apply))
      .asTwitter
}
object UserStore {
  def hashString(digest:MessageDigest)(s:String): String = {
    val hashBytes = digest.digest(s.getBytes(constants.encoding))
    new String(hashBytes, constants.encoding)
  }
  def securePassword(digest:MessageDigest)(u:User): User = {
    u.copy(hashedPassword = hashString(digest)(u.hashedPassword))
  }
}
