package webmodelica.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.twitter.finatra.json.modules.FinatraJacksonModule
import webmodelica.WMSpec
import webmodelica.core.AppModule
import webmodelica.models._

class SessionRegistrySpec extends WMSpec {
  val conf = AppModule.configProvider.mope

  "The SessionRegistry" should "assign unique ids" in {
    val registry = new SessionRegistry(conf)
    val sessions = Seq(registry.create(Project(ProjectRequest("nico", "awesome project", com.twitter.finagle.http.Request()))),
      registry.create(Project(ProjectRequest("nico", "awesome project", com.twitter.finagle.http.Request()))),
      registry.create(Project(ProjectRequest("nico", "awesome project", com.twitter.finagle.http.Request()))))

    val set = sessions.toSet
    set should have size sessions.length
    forAll(sessions) { s => set(s) }
  }
  it should "retrieve sessions" in {
    val registry = new SessionRegistry(conf)
    val (_, session) = registry.create(Project(ProjectRequest("nico", "awesome project", com.twitter.finagle.http.Request())))
    registry.get(session.idString) should not be empty
  }
}
