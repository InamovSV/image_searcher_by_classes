package com.isv.img_search_api.db

import com.isv.img_search_api.Model.Epic
import slick.jdbc.PostgresProfile.api._
import com.isv.img_search_api.db.EpicTable.Epics

trait EpicRepo extends PostgresBaseRepository {

  val epicRepo: EpicRepo

  class EpicRepo extends BaseRepository[String, Epic, EpicTable] {
    override val query: TableQuery[EpicTable] = Epics
  }
}
