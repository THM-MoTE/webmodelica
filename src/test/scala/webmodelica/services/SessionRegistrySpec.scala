package webmodelica.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.twitter.finatra.json.modules.FinatraJacksonModule
import com.twitter.finagle.stats.NullStatsReceiver
import com.twitter.util._
import webmodelica.WMSpec
import webmodelica.core.AppModule
import webmodelica.models._

class SessionRegistrySpec extends WMSpec {
  "The SessionRegistryImpl" should "assign unique ids" in {
    val registry = new InMemorySessionRegistry(appConf, new NullStatsReceiver())
    val sessions = Await.result(Future.collect(Seq(registry.create(Project(ProjectRequest("nico", "awesome project", com.twitter.finagle.http.Request()))),
      registry.create(Project(ProjectRequest("nico", "awesome project", com.twitter.finagle.http.Request()))),
      registry.create(Project(ProjectRequest("nico", "awesome project", com.twitter.finagle.http.Request()))))))

    val set = sessions.toSet
    set should have size sessions.length
    forAll(sessions) { s => set(s) }
  }
  it should "retrieve sessions" in {
    val registry = new InMemorySessionRegistry(appConf, new NullStatsReceiver())
    val (_, session) = Await.result( registry.create(Project(ProjectRequest("nico", "awesome project", com.twitter.finagle.http.Request()))) )
    Await.result(registry.get(session.idString)) should not be empty
  }
}
