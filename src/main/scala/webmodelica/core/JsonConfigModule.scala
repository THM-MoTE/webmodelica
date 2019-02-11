package webmodelica.core

import com.twitter.finatra.json.modules._
import com.fasterxml.jackson.databind.PropertyNamingStrategy

object JsonConfigModule extends FinatraJacksonModule {
  override val propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE
}
