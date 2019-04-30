package webmodelica

object constants {
  import java.nio.charset.StandardCharsets
  val encoding = StandardCharsets.UTF_8
  val projectCollection = "projects"
  val userCollection = "users"
  val authorizationHeader = "Authorization"

  val cacheRootSuffix = "webmodelica:"
  val userCacheSuffix = cacheRootSuffix+"users"
  val completionCacheSuffix = cacheRootSuffix+"completions"
}
