package com.isv.img_search_api

import java.util.UUID

import akka.http.scaladsl.server.{RequestContext, Route}
import cats.data.NonEmptyList
import com.isv.img_search_api.Errors.CommonError.ApplicationError
import com.isv.img_search_api.Model.{EpicStatuses, Task}
import com.isv.img_search_api.Model.EpicStatuses.EpicStatus
import com.isv.img_search_api.Model.TaskStatuses.TaskStatus
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Codec
import io.circe.generic.JsonCodec
import io.circe.generic.semiauto.deriveCodec
import io.scalaland.chimney.dsl._
import akka.http.scaladsl.server.RouteConcatenation._

import scala.concurrent.Future
package object api {

  def genUUID: String = UUID.randomUUID().toString

  def concatRoutes(token: String)(route: (String => Route), routes: (String => Route)*): Route = {
    (route +: routes).map(_(token)).reduceLeft(_ ~ _)
  }

  @JsonCodec
  case class LoginRequest(email: String, password: String)

  @JsonCodec
  case class CreateTaskRequest(urls: List[String],
                               container: String = "body",
                               regex: Option[String],
                               className: String)

  @JsonCodec
  case class CreateTasksResponse(uuid: String, status: EpicStatus = EpicStatuses.InProgress)

  @JsonCodec
  case class TaskInfo(uuid: String,
                      status: EpicStatus,
                      container: String = "body",
                      regex: Option[String],
                      className: String,
                      subTasks: Seq[SubTask])

  @JsonCodec
  case class SubTask(url: String, status: TaskStatus)

  object SubTask{
    def fromTask(task: Task): SubTask = task.into[SubTask].transform
  }

  object ErrorDuringRegistration{
    final val EmailAlreadyExists = "error.valida.during-registration.email-already-exists"
  }

  final val ErrorEmailMissing = "error.jwt.missing-email-field"
  final val ErrorImageClassNotFound = "error.image-class.not-found"
}
