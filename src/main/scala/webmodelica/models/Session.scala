package webmodelica.models

import webmodelica.stores.FSStore
import java.util.UUID
import io.scalaland.chimney.dsl._

import webmodelica.UUIDStr

case class Session(
  project: Project,
  id: UUID = UUID.randomUUID(),
) {
  def idString: String = id.toString
  def owner:String = project.owner
}

case class JSSession(project: JSProject,
                    id: UUIDStr)

object JSSession {
  def apply(s: Session): JSSession =
    s.into[JSSession]
      .withFieldComputed(_.id, _.idString)
      .withFieldComputed(_.project, s => JSProject(s.project))
      .transform
}
