package com.isv.img_search_api.api

import akka.event.Logging
import akka.http.scaladsl.server.{Directives, PathMatchers}
import akka.http.scaladsl.server.directives.DebuggingDirectives
import cats.data.{NonEmptyList, OptionT}
import com.isv.img_search_api.Model.Epic
import com.isv.img_search_api.db.{EpicRepo, ImageClassRepo, TaskRepo}
import com.isv.img_search_api.publishers.TaskProducer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import cats.instances.future._

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait TaskRoute {
  self: JwtParser
    with EpicRepo
    with TaskRepo
    with ImageClassRepo
    with TaskService
    with TaskProducer =>


  class TaskRoute extends Directives with FailFastCirceSupport with JwtParser {

    def route = {
      DebuggingDirectives.logRequestResult("Client ReST", Logging.InfoLevel)(tasks)
    }

    private def tasks = pathPrefix("tasks") {
      authenticated { token =>
        concatRoutes(token)(createTask, getTaskInfo)
      }
    }

    private def createTask(token: String) = post {
      entity(as[CreateTaskRequest]) { request =>
        onComplete(taskService.tasks(request, token)) {
          case Failure(exception) => complete(exception)
          case Success((epic, tasks)) =>
            val loadedTasks = for {
              _ <- epicRepo.insert(epic)
              _ <- taskRepo.insert(tasks)
              _ <- Future.sequence(tasks.map(taskProducer.sendTask))
            } yield ()

            onComplete(loadedTasks)(_ => complete(CreateTasksResponse(epic.uuid)))
        }
      }
    }

    private def getTaskInfo(token: String) = pathPrefix(Segment) { uuid =>
      val loadInfo = (for {
        epic <- epicRepo.getById(uuid)
        imgClass <- imageClassRepo.getById(epic.classUUID)
        tasks <- taskRepo.findByEpicUUID(epic.uuid)
        subtasks = tasks.map(SubTask.fromTask)
      } yield TaskInfo(epic.uuid, epic.status, epic.container, epic.regex, imgClass.name, subtasks))
      complete(loadInfo)
    }

  }

}
