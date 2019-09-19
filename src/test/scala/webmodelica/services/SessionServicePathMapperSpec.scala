package webmodelica.services

import better.files._
import webmodelica.models._
import webmodelica.models.config._
import java.nio.file._
import com.twitter.finagle.stats.NullStatsReceiver

class SessionServicePathMapperSpec
    extends webmodelica.WMSpec {

  val tmpDir = File.newTemporaryDirectory("mapperspec-tmp")
  val conf = MopeClientConfig("http://localhost:9015/", 800, MopeDataConfig(tmpDir.path, Paths.get("/data/wm")))
  val session = Session(Project("nico", "testproj"))
  val service = new SessionService(conf, session,appConf.redis, new NullStatsReceiver(), module.httpClient)
  val mapper = service.pathMapper
  val bindRoot = conf.data.bindDirectory.resolve(session.basePath)
  val hostRoot = service.fsStore.rootDir

  override def afterAll:Unit = tmpDir.delete()

  "The PathMapper" should "convert relative path's to docker-bind path's" in {
    val relative = "a/b/c/test.mo"
    mapper.toBindPath(Paths.get(relative)) shouldBe bindRoot.resolve(relative)
  }
  it should "convert absolute path's to docker-bind path's" in {
    val relative = "a/b/c/test.mo"
    val absolute = hostRoot.resolve(relative)
    mapper.toBindPath(absolute) shouldBe bindRoot.resolve(relative)
  }
  it should "convert path's with spaces to docker-bind path's" in {
    val relative = "a/b/c x d/test.mo"
    val absolute = hostRoot.resolve(relative)
    mapper.toBindPath(absolute) shouldBe bindRoot.resolve(relative)
  }
  it should "convert docker-bind path's to host path's" in {
    val relative = "a/b/c/test.mo"
    mapper.toHostPath(bindRoot.resolve(relative)) shouldBe hostRoot.resolve(relative)
  }
  it should "rouned-trippin' when converting host => bind => host" in {
    val absolute = hostRoot.resolve("a/b/c/test.mo")
    val fn: Path => Path = mapper.toBindPath _ andThen mapper.toHostPath _
    fn(absolute) shouldBe absolute
  }
  it should "relativize bind-path's" in {
    val relative = "a/b/c/test.mo"
    val absolute = bindRoot.resolve(relative)
    mapper.relativize(absolute) shouldBe Paths.get(relative)
  }
}
