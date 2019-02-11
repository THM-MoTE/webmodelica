package webmodelica.controllers

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.google.inject.Inject
import webmodelica.models.config._

case class Infos(
  config: WMConfig,
  appName:String,
  version:String,
  license:String,
  commitHash:String,
)

class InfoController @Inject()(config: WMConfig)
    extends Controller {

  import buildinfo.BuildInfo

  get("/info") { _:Request =>
    Infos(
      config,
      BuildInfo.name,
      BuildInfo.version,
      BuildInfo.license,
      BuildInfo.commit
    )
  }
}
