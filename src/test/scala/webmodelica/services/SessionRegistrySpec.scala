package webmodelica.services

import com.twitter.finagle.stats.NullStatsReceiver
import com.twitter.util._
import webmodelica.WMSpec
import webmodelica.models._

class SessionRegistrySpec extends WMSpec {
  "The SessionRegistryImpl" should "assign unique ids" in {
    val registry = new InMemorySessionRegistry(appConf, new NullStatsReceiver())
    val sessions = Await.result(Future.collect(Seq(registry.create(Project("nico", "awesome project")),
      registry.create(Project("nico", "awesome project")),
      registry.create(Project("nico", "awesome project")))))

    val set = sessions.toSet
    set should have size sessions.length
    forAll(sessions) { s => set(s) }
  }
  it should "retrieve sessions" in {
    val registry = new InMemorySessionRegistry(appConf, new NullStatsReceiver())
    val (_, session) = Await.result( registry.create(Project("nico", "awesome project")) )
    Await.result(registry.get(session.idString)) should not be empty
  }
}
