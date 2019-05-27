/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.models

import webmodelica.stores.FSStore
import java.util.UUID
import io.scalaland.chimney.dsl._
import webmodelica.models._
import webmodelica.UUIDStr
import io.circe.generic.JsonCodec

@JsonCodec
case class Session(
  project: Project,
  id: UUID = UUID.randomUUID(),
  mopeId: Option[Int] = None
) {
  def idString: String = id.toString
  def owner:String = project.owner
  def basePath:String = s"${owner}_${project.name}"
}

@JsonCodec
case class JSSession(project: JSProject,
  id: UUIDStr,
  files: List[ModelicaPath])

object JSSession {
  def apply(s: Session, files: List[ModelicaPath]=List.empty): JSSession =
    s.into[JSSession]
      .withFieldComputed(_.id, _.idString)
      .withFieldComputed(_.project, s => JSProject(s.project))
      .withFieldConst(_.files, files)
      .transform
}
