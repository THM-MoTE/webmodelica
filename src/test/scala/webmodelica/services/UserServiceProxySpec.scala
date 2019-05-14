package webmodelica.services

import com.twitter.util.Await
import webmodelica.WMSpec
import webmodelica.models.User

class UserServiceProxySpec extends WMSpec {
  val proxy = new UserServiceProxy(appConf.userService)

  val user = User("test-2-user-5", "test-2-user-5@xample.org", None, None, "1234")

  "The user proxy" should "create a user in the user service" in {
    Await.result(proxy.add(user))
  }
  it should "find a user by it's username" in {
    Await.result(proxy.findBy(user.username)).get.toAuthUser shouldBe user.toAuthUser
  }
}
