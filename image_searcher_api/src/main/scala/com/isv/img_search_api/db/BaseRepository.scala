package com.isv.img_search_api.db

import com.isv.img_search_api.Exceptions.ApplicationException
import com.isv.img_search_api.Model.HasId
import slick.ast.TypedType
import slick.jdbc.PostgresProfile.api._
import slick.basic.DatabaseConfig
import slick.jdbc.PostgresProfile
import com.isv.img_search_api.Errors.CommonError._

import scala.concurrent.{ExecutionContext, Future}

trait BaseTable[E <: HasId[PK], PK] {
  self: Table[E] =>
  val id: Rep[PK]
}

trait PostgresBaseRepository {

  def dbc: DatabaseConfig[PostgresProfile]

  def db = dbc.db

  implicit val ec: ExecutionContext

  abstract class BaseRepository[PK: BaseColumnType, E <: HasId[PK], T <: Table[E] with BaseTable[E, PK]]
    extends GenericRepo[PK, E] {

    val query: TableQuery[T]

    def insert(item: E): Future[Int] = db.run(query += item)

    def insert(items: Seq[E]): Future[Option[Int]] = db.run(query ++= items)

    def insertWithIdQuery(item: E): Future[PK] =
      db.run(query returning query.map(_.id) += item)

    override def insertWithEntity(item: E): Future[E] =
      db.run((query returning query) += item)

    def findById(id: PK): Future[Option[E]] = db.run(query.filter(_.id === id).result.headOption)

    def getById(id: PK): Future[E] = db.run(query.filter(_.id === id).result.headOption).flatMap {
      case Some(value) => Future.successful(value)
      case None => Future.failed(ApplicationException(s"${classOf[E].getSimpleName}.$EntityNotFoundError"))
    }

    def findBy(predicate: T => Rep[Boolean]): Future[Seq[E]] =
      db.run(query.filter(predicate).result)

    def update(item: E): Future[Int] =
      db.run(query.filter(_.id === item.id).update(item))

    def deleteById(id: PK): Future[Int] = db.run(query.filter(_.id === id).delete)

    override def findAll(): Future[Seq[E]] = db.run(query.result)
  }

}
