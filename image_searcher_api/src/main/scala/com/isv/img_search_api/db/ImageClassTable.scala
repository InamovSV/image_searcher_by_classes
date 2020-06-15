package com.isv.img_search_api.db

import com.isv.img_search_api.Model.{ImageClass, ImageClassAccesses}
import com.isv.img_search_api.Model.ImageClassAccesses.ImageClassAccess
import slick.jdbc.PostgresProfile.api._
import slick.collection.heterogeneous.HNil

import scala.language.implicitConversions

class ImageClassTable(tag: Tag) extends Table[ImageClass](tag, "image_class") with BaseTable[ImageClass, String] {
  import ImageClassTable._

  override val id = column[String]("uuid", O.PrimaryKey)
  val name = column[String]("name")
  val path = column[String]("path")
  val owner = column[String]("owner_uuid")
  val access = column[ImageClassAccess]("access")

  override def * = (id :: name :: path :: owner :: access :: HNil).mapTo[ImageClass]
}

object ImageClassTable {
  val ImageClasses = TableQuery[ImageClassTable]

  implicit val epicStatusMapper: BaseColumnType[ImageClassAccess] = {
    MappedColumnType.base[ImageClassAccess, String](_.toString, ImageClassAccesses.withName)
  }
}