package webmodelica.models

import webmodelica.stores.FSStore
import java.util.UUID

case class Session(
  owner: String,
  project: Project,
  id: UUID = UUID.randomUUID(),
)
