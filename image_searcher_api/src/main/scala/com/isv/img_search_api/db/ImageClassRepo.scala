package com.isv.img_search_api.db

import com.isv.img_search_api.Model.ImageClass
import com.isv.img_search_api.db.ImageClassTable.ImageClasses
import slick.jdbc.PostgresProfile.api._

trait ImageClassRepo extends PostgresBaseRepository {

  val imageClassRepo: ImageClassRepo

  class ImageClassRepo extends BaseRepository[String, ImageClass, ImageClassTable] {
    override val query: TableQuery[ImageClassTable] = ImageClasses

    def findByName(name: String) = db.run(query.filter(_.name === name).result.headOption)
  }
}
