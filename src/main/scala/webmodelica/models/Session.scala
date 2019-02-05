package webmodelica.models

import webmodelica.stores.FSStore

case class Session(
  owner: String,
  project: Project,
  fsStore: FSStore,
)
