package com.isv.img_search_api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import authentikat.jwt.JwtHeader
import com.isv.img_search_api
import com.isv.img_search_api.api.{AuthRoute, JwtParser}
import com.isv.img_search_api.db.UserRepo
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import slick.basic.DatabaseConfig
import slick.jdbc.PostgresProfile
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

object App extends App
  with AuthRoute
  with JwtParser
  with UserRepo
  with LazyLogging {

  val config = ConfigFactory.load()

  implicit val system = ActorSystem("image_searcher_api_system")
  override val api: Api = new Api
  override val tokenExpiryPeriodInDays: Int = config.getInt("api.token-expiry-period-in-days")
  override val header: JwtHeader = JwtHeader("HS256")
  override val secretKey: String = config.getString("api.secret")
  override val userRepo: UserRepo = new UserRepo
  override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  override val dbc: DatabaseConfig[PostgresProfile] = DatabaseConfig.forConfig("slick", config)

  Http().bindAndHandle(api.route, "0.0.0.0", 9000).onComplete {
    case Failure(exception) => logger.error("Failed to start server", exception)
    case Success(binding) =>
      logger.info("Server up")
      sys.addShutdownHook {
        Await.ready(binding.unbind(), 3 seconds)
        Await.ready(system.terminate(), 3 seconds)
      }
  }
}

