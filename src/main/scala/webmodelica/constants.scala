package webmodelica

object constants {
  import java.nio.charset.StandardCharsets
  val encoding = StandardCharsets.UTF_8
  val projectCollection = "projects"
  val userCollection = "users"
  val authorizationHeader = "Authorization"
  val authenticationHeader = "Authentication"

  val cacheRootSuffix = "wm:"
  val userCacheSuffix = cacheRootSuffix+"users"
  val completionCacheSuffix = cacheRootSuffix+"completions"
}
