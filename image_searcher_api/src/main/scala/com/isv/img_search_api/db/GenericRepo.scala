package com.isv.img_search_api.db

import com.isv.img_search_api.Model.HasId

import scala.concurrent.Future

trait GenericRepo[PK, E <: HasId[PK]] {

  def insert(item: E): Future[Int]

  def insert(items: Seq[E]): Future[Option[Int]]

  def insertWithIdQuery(item: E): Future[PK]

  def insertWithEntity(item: E): Future[E]

  def findById(id: PK): Future[Option[E]]

  def update(item: E): Future[Int]

  def deleteById(id: PK): Future[Int]

  def findAll(): Future[Seq[E]]
}
