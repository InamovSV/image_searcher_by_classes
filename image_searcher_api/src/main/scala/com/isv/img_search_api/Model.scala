package com.isv.img_search_api

import com.isv.img_search_api.Model.EpicStatuses.EpicStatus
import com.isv.img_search_api.Model.ImageClassAccesses.ImageClassAccess
import com.isv.img_search_api.Model.TaskStatuses.TaskStatus

object Model {

  trait HasId[PK] {
    def id: PK
  }

  case class Task(uuid: String,
                  epicUUID: String,
                  url: String,
                  status: TaskStatus = TaskStatuses.InProgress) extends HasId[String]{
    override def id: String = uuid
  }

  object TaskStatuses extends Enumeration {
    type TaskStatus = Value

    val Complete = Value("COMPLETE")
    val InProgress = Value("IN_PROGRESS")
    val Failed = Value("FAILED")
  }

  case class Epic(uuid: String,
                  userToken: String,
                  container: String = "body",
                  regex: Option[String],
                  classUUID: String,
                  status: EpicStatus = EpicStatuses.InProgress) extends HasId[String]{
    def id: String = uuid
  }

  object EpicStatuses extends Enumeration {
    type EpicStatus = Value

    val Complete = Value("COMPLETE")
    val PartialComplete = Value("PARTIAL_COMPLETE")
    val InProgress = Value("IN_PROGRESS")
    val Failed = Value("FAILED")
  }

  case class User(token: String, password: String, email: String) extends HasId[String] {
    override def id: String = token
  }

  case class ImageClass(uuid: String, name: String, path: String, ownerToken: String, access: ImageClassAccess) extends HasId[String] {
    override def id: String = uuid
  }

  object ImageClassAccesses extends Enumeration{
    type ImageClassAccess = Value

    val Public = Value("PUBLIC")
    val Private = Value("PRIVATE")
  }

}
