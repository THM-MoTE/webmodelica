package webmodelica.models

import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{ fromRegistries, fromProviders }


trait DocumentWriters {
  val codecRegistry = fromRegistries(fromProviders(classOf[Project]), fromProviders(classOf[UserDocument]), DEFAULT_CODEC_REGISTRY)
}
