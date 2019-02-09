package webmodelica.models

/** DataTypes for communicating with the mope-server.
  * They are all originated from the mope-server repository:
  * https://github.com/THM-MoTE/mope-server
  */
package object mope {
  /** Wrapper around a path to a file. */
  case class FilePath(path: String) extends AnyVal
  /** A position (2D Point) inside of a file */
  case class FilePosition(line: Int, column: Int)
}
