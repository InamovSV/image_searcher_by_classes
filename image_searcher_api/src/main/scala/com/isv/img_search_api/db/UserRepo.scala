package com.isv.img_search_api.db

import com.isv.img_search_api.Exceptions.ApplicationException
import com.isv.img_search_api.Model.User
import slick.jdbc.PostgresProfile.api._
import com.isv.img_search_api.db.UserTable.Users

import scala.concurrent.{ExecutionContext, Future}

trait UserRepo extends PostgresBaseRepository {

  val userRepo: UserRepo
  implicit val ec: ExecutionContext

  class UserRepo extends BaseRepository[String, User, UserTable] {
    override val query: TableQuery[UserTable] = Users

    def addNewUser(user: User) = {
      findByEmail(user.email).flatMap {
        case None => insert(user)
        case Some(_) => Future.failed(ApplicationException(DuplicateEmailError))
      }
    }

    def findByEmail(email: String) =
      db.run(query.filter(_.email === email).result.headOption)
  }

}
