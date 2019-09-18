/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.models

import org.mongodb.scala.bson.BsonObjectId
import io.scalaland.chimney.dsl._
import io.circe.generic.JsonCodec
import webmodelica.controllers.AkkaProjectController.ProjectRequest

@JsonCodec
case class Project(
  _id: String,
  owner: String,
  name: String,
  visibility:String = Project.privateVisibility
)

@JsonCodec
case class JSProject(
  id: String,
  owner: String,
  name: String,
  visibility:String)

object JSProject {
  def apply(p:Project): JSProject = {
    require(p != null, "project can't be null!")
    p.into[JSProject].withFieldRenamed(_._id, _.id).transform
  }
}

object Project {

  val publicVisibility = "public"
  val privateVisibility = "private"
  val visibilities = Set(publicVisibility, privateVisibility)

  def isAvailableVisibility(s:String):Boolean = visibilities contains s

  def apply(request: ProjectRequest): Project =
    request.into[Project]
      .withFieldComputed(_._id, req => s"${req.owner}_${req.name}")
      .withFieldComputed(_.visibility, _ => privateVisibility)
      .transform
  def apply(owner:String, name:String): Project =
    Project(s"${owner}_${name}", owner, name, privateVisibility)
}
