/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.models

import com.twitter.util.Future
import akka.http.scaladsl.model._

object errors {
  def notFoundExc[A](reason:String)(opt: Option[A]): Future[A] = opt match {
    case Some(a) => Future.value(a)
    case _ => Future.exception(NotFoundException(reason))
  }

  case class NotFoundException(reason:String) extends WMException {
    override def status: StatusCode = StatusCodes.NotFound
    override def getMessage: String = reason
  }

  sealed abstract class WMException extends scala.Exception {
    //for whatever reason this must be a abstract class..
    //guice doesn't know how to handle traits ..

    def status: StatusCode = StatusCodes.InternalServerError
  }
  private[errors] trait BRWMException extends WMException {
    override def status: StatusCode = StatusCodes.BadRequest
  }

  trait AlreadyInUse extends WMException {
    def resource:String
    def name:String
    override def status: StatusCode = StatusCodes.Conflict
    override def getMessage: String = s"$resource `$name` already assigned"
  }

  case object UserAlreadyInUse extends AlreadyInUse {
    override def name:String = "username or email"
    override def resource:String = "User:"
  }
  case class UsernameAlreadyInUse(override val name:String) extends AlreadyInUse {
    override def resource:String = "Username"
  }
  case class ProjectnameAlreadyInUse(override val name:String) extends AlreadyInUse {
    override def resource:String = "Projectname"
  }
  case class FileAlreadyInUse(override val name:String) extends AlreadyInUse {
    override def resource:String = "File"
  }

  case object CredentialsError extends WMException {
    override def status: StatusCode = StatusCodes.Unauthorized
    override def getMessage: String = "Wrong username or password!"
  }
  case class ResourceUsernameError(resourceName:String="resource") extends BRWMException {
    override def getMessage: String = s"You can't create a ${resourceName} for another user!"
  }
  case object DeleteUsernameError extends BRWMException {
    override def getMessage: String = "You can't delete a project from another user!"
  }
  case object VisibilityUsernameError extends BRWMException {
    override def getMessage: String = "You can't change the visibility of another users project!"
  }
  case object VisibilityProjectError extends BRWMException {
    override def getMessage: String = "You are denied access to this project!"
  }
  case class ArchiveError(reason:String) extends WMException {
    override def getMessage: String = reason
  }
  case class StepSizeCalculationError(reason:String) extends BRWMException {
    override def getMessage: String = reason
  }
  case class MopeServiceError(reason:String) extends WMException {
    override def getMessage: String = reason
  }
  case class UserServiceError(reason:String) extends WMException {
    override def getMessage: String = reason
  }
  case class SimulationSetupError(reason:String) extends BRWMException {
    override def getMessage: String = reason
  }
  case object SimulationNotFinished extends WMException {
    override def status: StatusCode = StatusCodes.Conflict
    override def getMessage: String = s"simulation not finished!"
  }
}
