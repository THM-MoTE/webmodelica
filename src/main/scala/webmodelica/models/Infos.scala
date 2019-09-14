package webmodelica.models

import io.circe.generic.JsonCodec
import buildinfo.BuildInfo

@JsonCodec
case class Infos(
  appName:String,
  version:String,
  copyright:String,
  license:String,
  licenseUri:String,
  commitHash:String,
)

object Infos {
  def apply():Infos = Infos(
    BuildInfo.name,
    BuildInfo.version,
    BuildInfo.copyright,
    BuildInfo.license,
    BuildInfo.licenseUri,
    BuildInfo.commit
  )
}
