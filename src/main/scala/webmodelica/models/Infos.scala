/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.models

import io.circe.generic.JsonCodec
import buildinfo.BuildInfo

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
  def apply():Infos = Infos(
    BuildInfo.name,
    BuildInfo.version,
    BuildInfo.copyright,
    BuildInfo.license,
    BuildInfo.licenseUri,
    BuildInfo.commit
  )
}
