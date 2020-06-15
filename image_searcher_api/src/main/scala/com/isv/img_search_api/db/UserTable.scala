package com.isv.img_search_api.db

import com.isv.img_search_api.Model.User
import slick.jdbc.PostgresProfile.api._
import slick.collection.heterogeneous.HNil

class UserTable(tag: Tag) extends Table[User](tag, "user") with BaseTable[User, String] {

  override val id: Rep[String] = column[String]("token", O.PrimaryKey)
  val password = column[String]("password")
  val email = column[String]("email", O.Unique)

  override def * = (id :: password :: email :: HNil).mapTo[User]
}

object UserTable {
  val Users = TableQuery[UserTable]
}
