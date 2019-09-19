package webmodelica.stores

import com.twitter.util.Await
import webmodelica.controllers.AkkaProjectController
import webmodelica.models.{Project, User}
import webmodelica.models.errors.UsernameAlreadyInUse
import webmodelica.{DBSpec, WMSpec, constants}

import scala.util.Try

class ProjectStoreSpec extends DBSpec(Some(constants.projectCollection)) {
  val store = new ProjectStore(database)
  val user = User("test-1", "test-1@test.example", "1234")
  val project = Project(user.username, "test-1-project")

  "The ProjectStore" should "save a project" in {
    Await.result(store.add(project))
    succeed
  }

  it should "find all projects for a user" in {
    val projects = Await.result(store.byUsername(user.username))
    forAll(projects) { p =>
      if(! (p.owner == user.username || p.visibility == Project.publicVisibility) ) {
        fail(s"project should either be owned by the user or have public visibility: user $user, project: $p")
      }
    }
  }
  it should "find a project by it's id" in {
    val proj = Await.result(store.find(project._id).map(_.get))
    proj shouldBe project
  }

  it should "update project's visibility" in {
    val p = Await.result(store.setVisiblity(project._id, Project.publicVisibility))
    p.visibility shouldBe Project.publicVisibility
    val p2 = Await.result(store.find(p._id).map(_.get))
    p2.visibility shouldBe Project.publicVisibility
  }
  it should "reject arbitrary visiblities" in {
    an [IllegalArgumentException] should be thrownBy Await.result(store.setVisiblity(project._id, "testi1"))
  }
  it should "delete a project" in {
    Await.result(store.delete(project._id))
    succeed
  }
}
