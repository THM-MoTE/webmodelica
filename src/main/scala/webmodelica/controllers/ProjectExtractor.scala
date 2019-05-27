/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.controllers

import com.twitter.util.Future
import webmodelica.models.{Project, errors}
import webmodelica.stores.ProjectStore

trait ProjectExtractor {
  def projectStore: ProjectStore

  def extractProject(id:String, username:String): Future[Project] =
    projectStore.findBy(id, username).flatMap(errors.notFoundExc(s"project with $id not found!"))
}
