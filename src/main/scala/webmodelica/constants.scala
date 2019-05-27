/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica

object constants {
  import java.nio.charset.StandardCharsets
  val encoding = StandardCharsets.UTF_8
  val projectCollection = "projects"
  val userCollection = "users"
  val authorizationHeader = "Authorization"
  val authenticationHeader = "Authentication"

  val cacheRootSuffix = "wm:"
  val userCacheSuffix = cacheRootSuffix+"users"
  val completionCacheSuffix = cacheRootSuffix+"completions"
}
