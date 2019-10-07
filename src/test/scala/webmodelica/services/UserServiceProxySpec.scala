/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.services

import com.twitter.util.Await
import webmodelica.WMSpec
import webmodelica.models.User
import webmodelica.models.errors.{
  UserServiceError,
  UserAlreadyInUse
}

class UserServiceProxySpec extends WMSpec {
  val proxy = new UserServiceProxy(appConf.userService)(module.actorSystem)

  val user = User("test-2-user-5", "test-2-user-5@xample.org", None, None, "1234")

  "The user proxy" should "create a user in the user service" in {
    Await.result(proxy.add(user).handle {
      case UserAlreadyInUse => ()
    })
  }
  it should "find a user by it's username" in {
    Await.result(proxy.findBy(user.username)).get.toAuthUser shouldBe user.toAuthUser
  }
}
