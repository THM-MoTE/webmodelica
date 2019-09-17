/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica

import java.nio.file.{Path, Paths}
import java.util.UUID

package object models extends JsonSupport {
  implicit class RichPath(val p:Path) extends AnyVal {
    def asModelicaPath: ModelicaPath = ModelicaPath(p)
  }
  implicit class RichString(val s:String) extends AnyVal {
    def asModelicaPath: ModelicaPath = Paths.get(s).asModelicaPath
    def asPath: Path = Paths.get(s)
  }
}
