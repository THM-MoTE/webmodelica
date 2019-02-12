package webmodelica.models

object errors {
  case class UsernameAlreadyInUse(name:String) extends RuntimeException {
    override def getMessage: String = s"Username `$name` already assigned"
  }
}
