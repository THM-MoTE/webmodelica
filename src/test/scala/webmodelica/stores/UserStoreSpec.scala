package webmodelica.stores

import com.twitter.util.Await
import webmodelica.models.User
import webmodelica.models.errors.UsernameAlreadyInUse
import webmodelica.{DBSpec, WMSpec, constants}

class UserStoreSpec extends DBSpec(Some(constants.userCollection)) {
  val store  = new UserStore(database)

  val user = User("nico", "nico@test.example", "123456")
  "The UserStore" should "save users" in {
    Await.result(store.add(user))
  }
  it should "find the user by name" in {
    Await.result(store.findBy(user.username)).get shouldBe user
  }
  it should "return none if username not defined" in {
    Await.result(store.findBy("blup")) shouldBe None
  }
  it should "throw an UsernameAlreadyInUse" in {
    val user2 = user.copy(email="nico2@test.example")
    an [UsernameAlreadyInUse] shouldBe thrownBy (Await.result(store.add(user2)))
  }
}
