package com.isv.img_search_api.db

import com.isv.img_search_api.Model.{Task, TaskStatuses}
import com.isv.img_search_api.Model.TaskStatuses.TaskStatus
import com.isv.img_search_api.db.EpicTable.Epics
import slick.jdbc.PostgresProfile.api._
import slick.collection.heterogeneous.HNil

class TaskTable(tag: Tag) extends Table[Task](tag, "task") with BaseTable[Task, String] {

  import TaskTable._

  override val id: Rep[String] = column[String]("uuid", O.PrimaryKey)
  val epicId = column[String]("epic_uuid")
  val url = column[String]("url")
  val status = column[TaskStatus]("status")

  def epicIdFk = foreignKey(
    "SUP_FK",
    epicId,
    Epics
  )(
    _.id,
    onUpdate = ForeignKeyAction.Restrict,
    onDelete = ForeignKeyAction.Cascade
  )

  override def * = (id :: epicId :: url :: status :: HNil).mapTo[Task]
}

object TaskTable {
  val Tasks = TableQuery[TaskTable]

  implicit val epicStatusMapper: BaseColumnType[TaskStatus] = {
    MappedColumnType.base[TaskStatus, String](_.toString, TaskStatuses.withName)
  }
}