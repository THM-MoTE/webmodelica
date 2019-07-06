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
import webmodelica.models.config._
import io.circe.generic.JsonCodec

@JsonCodec
case class Infos(
  appName:String,
  version:String,
  copyright:String,
  license:String,
  licenseUri:String,
  commitHash:String,
)
object Infos {
  import buildinfo.BuildInfo

  def apply():Infos = Infos(
    BuildInfo.name,
    BuildInfo.version,
    BuildInfo.copyright,
    BuildInfo.license,
    BuildInfo.licenseUri,
    BuildInfo.commit
  )
}

class InfoController @Inject()(config: WMConfig, prefix:webmodelica.ApiPrefix)
    extends Controller {

  prefix(prefix.p) {
    get("/info") { _:Request => Infos() }
  }
}
