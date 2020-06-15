package com.isv.img_search_api.publishers

import com.isv.img_search_api.Model.Task
import com.isv.img_search_api.db.TaskRepo

import scala.concurrent.Future

trait TaskProducer { self: TaskRepo =>

  val taskProducer: TaskProducer

  class TaskProducer{

    def sendTask(task: Task): Future[Task] = ???
  }
}
