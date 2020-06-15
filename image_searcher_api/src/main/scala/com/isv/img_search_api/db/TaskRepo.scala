package com.isv.img_search_api.db

import TaskTable.Tasks
import com.isv.img_search_api.Model.Task
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.PostgresProfile.api._

trait TaskRepo extends PostgresBaseRepository {

  val taskRepo = new TaskRepo
  class TaskRepo extends BaseRepository[String, Task, TaskTable] {
    override val query: TableQuery[TaskTable] = Tasks

    def findByEpicUUID(uuid: String) = findBy(_.id === uuid)
  }
}
