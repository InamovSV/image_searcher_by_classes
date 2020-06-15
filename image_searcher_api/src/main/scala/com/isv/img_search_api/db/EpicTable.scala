package com.isv.img_search_api.db

import com.github.tminglei.slickpg.PgArraySupport
import com.isv.img_search_api.Model.{Epic, EpicStatuses}
import com.isv.img_search_api.Model.EpicStatuses.EpicStatus
import slick.jdbc.PostgresProfile.api._
import slick.collection.heterogeneous.HNil

import scala.language.implicitConversions

class EpicTable(tag: Tag) extends Table[Epic](tag, "image_class") with BaseTable[Epic, String] {
  import EpicTable._

  override val id = column[String]("uuid", O.PrimaryKey)
  val userId = column[String]("user_uuid")
  val container = column[String]("container")
  val regex = column[String]("regex")
  val status = column[EpicStatus]("status")
  val classUUID = column[String]("image_class_uuid")

  override def * = (id :: userId :: container :: regex :: status :: HNil).mapTo[Epic]
}

object EpicTable {
  val Epics = TableQuery[EpicTable]

  implicit val epicStatusMapper: BaseColumnType[EpicStatus] = {
    MappedColumnType.base[EpicStatus, String](_.toString, EpicStatuses.withName)
  }
}
