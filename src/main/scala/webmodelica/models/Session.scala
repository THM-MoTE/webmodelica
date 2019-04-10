package webmodelica.models

import webmodelica.stores.FSStore
import java.util.UUID
import io.scalaland.chimney.dsl._
import io.circe.generic.JsonCodec
import webmodelica.UUIDStr

case class Session(
  project: Project,
  id: UUID = UUID.randomUUID(),
) {
  def idString: String = id.toString
  def owner:String = project.owner
  def basePath:String = s"${owner}_${project.name}"
}

@JsonCodec
case class JSSession(project: JSProject,
  id: UUIDStr,
  files: List[ModelicaFile])

object JSSession {
  def apply(s: Session, files: List[ModelicaFile]=List.empty): JSSession =
    s.into[JSSession]
      .withFieldComputed(_.id, _.idString)
      .withFieldComputed(_.project, s => JSProject(s.project))
      .withFieldConst(_.files, files)
      .transform
}
