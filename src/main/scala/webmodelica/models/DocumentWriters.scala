package webmodelica.models


import reactivemongo.bson.{
  BSONDocumentWriter, BSONDocumentReader, Macros, document
}

trait DocumentWriters {
  implicit val projectHandler = Macros.handler[Project]
}
