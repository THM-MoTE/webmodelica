/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.models

import com.google.common.net.MediaType
import com.twitter.finatra.http.exceptions.NotFoundException
import com.twitter.util.Future

object errors {
  def notFoundExc[A](reason:String)(opt: Option[A]): Future[A] = opt match {
    case Some(a) => Future.value(a)
    case _ => Future.exception(NotFoundException(reason))
  }

  trait AlreadyInUse extends RuntimeException {
    def resource:String
    def name:String
    override def getMessage: String = s"$resource `$name` already assigned"
  }

  case class UsernameAlreadyInUse(override val name:String) extends AlreadyInUse {
    override def resource:String = "Username"
  }
  case class ProjectnameAlreadyInUse(override val name:String) extends AlreadyInUse {
    override def resource:String = "Projectname"
  }

  case object CredentialsError extends RuntimeException {
    override def getMessage: String = "Wrong username or password!"
  }
  case class ResourceUsernameError(resourceName:String="resource") extends RuntimeException {
    override def getMessage: String = s"You can't create a ${resourceName} for another user!"
  }
  case class ArchiveError(reason:String) extends RuntimeException(reason)
  case class StepSizeCalculationError(reason:String) extends RuntimeException(reason)
  case class MopeServiceError(reason:String) extends RuntimeException(reason)
  case class UserServiceError(reason:String) extends RuntimeException(reason)
  case class SimulationSetupError(reason:String) extends RuntimeException(reason)
  case object SimulationNotFinished extends RuntimeException {
    override def getMessage: String = s"simulation not finished!"
  }
}
