package webmodelica.models

import webmodelica.stores.FSStore
import java.util.UUID

case class Session(
  project: Project,
  id: UUID = UUID.randomUUID(),
) {
  def idString: String = id.toString
  def owner:String = project.owner
}
