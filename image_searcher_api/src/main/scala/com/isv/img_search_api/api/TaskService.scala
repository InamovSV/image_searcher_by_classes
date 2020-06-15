package com.isv.img_search_api.api

import cats.data.OptionT
import com.isv.img_search_api.Model.{Epic, Task}
import com.isv.img_search_api.db.ImageClassRepo
import cats.instances.future._
import com.isv.img_search_api.Exceptions.ApplicationException

import scala.concurrent.{ExecutionContext, Future}

trait TaskService {
  self: ImageClassRepo =>

  implicit val ec: ExecutionContext
  val taskService: TaskService

  class TaskService {
    def tasks(request: CreateTaskRequest, userToken: String): Future[(Epic, List[Task])] = {
      import request._
      (for {
        imgClass <- OptionT(imageClassRepo.findByName(className))
        epic = Epic(genUUID, userToken, container, regex, imgClass.name)
      } yield {
        val tasks = urls.map(Task(genUUID, epic.uuid, _))
        (epic, tasks)
      }).value.flatMap {
        case Some(value) => Future.successful(value)
        case None => Future.failed(ApplicationException(ErrorImageClassNotFound))
      }
    }
  }

}
