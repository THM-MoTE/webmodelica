package webmodelica.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.twitter.finatra.json.modules.FinatraJacksonModule
import webmodelica.WMSpec
import webmodelica.core.AppModule
import webmodelica.models._

class SessionRegistrySpec extends WMSpec {
  val json = FinatraJacksonModule.provideCamelCaseFinatraObjectMapper(new ObjectMapper with ScalaObjectMapper)
  val conf = AppModule.configProvider.mope

  "The SessionRegistry" should "assign unique ids" in {
    val registry = new SessionRegistry(conf, json)
    val sessions = Seq(registry.create(Project(ProjectRequest("nico", "awesome project"))),
      registry.create(Project(ProjectRequest("nico", "awesome project"))),
      registry.create(Project(ProjectRequest("nico", "awesome project"))))

    val set = sessions.toSet
    set should have size sessions.length
    forAll(sessions) { s => set(s) }
  }
  it should "retrieve sessions" in {
    val registry = new SessionRegistry(conf, json)
    val id = registry.create(Project(ProjectRequest("nico", "awesome project")))
    registry.get(id) should not be empty
  }
}
