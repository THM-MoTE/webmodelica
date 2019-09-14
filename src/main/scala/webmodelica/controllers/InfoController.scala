/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.controllers

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.google.inject.Inject
import webmodelica.models.Infos
import webmodelica.models.config._

class InfoController @Inject()(config: WMConfig, prefix:webmodelica.ApiPrefix)
    extends Controller {

  prefix(prefix.p) {
    get("/info") { _:Request => Infos() }
  }
}
