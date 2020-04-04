/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.services

import com.twitter.util._
import webmodelica.WMSpec
import webmodelica.models._

class SessionRegistrySpec extends WMSpec {
  "The SessionRegistryImpl" should "assign unique ids" in {
    val registry = new InMemorySessionRegistry(appConf, NoCaching.cacheFactory, module.httpClient)
    val sessions = Await.result(Future.collect(Seq(registry.create(Project("nico", "awesome project")),
      registry.create(Project("nico", "awesome project")),
      registry.create(Project("nico", "awesome project")))))

    val set = sessions.toSet
    set should have size sessions.length
    forAll(sessions) { s => set(s) }
  }
  it should "retrieve sessions" in {
    val registry = new InMemorySessionRegistry(appConf, NoCaching.cacheFactory, module.httpClient)
    val (_, session) = Await.result( registry.create(Project("nico", "awesome project")) )
    Await.result(registry.get(session.idString)) should not be empty
  }
}
