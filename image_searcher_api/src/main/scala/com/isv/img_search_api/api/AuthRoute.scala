package com.isv.img_search_api.api

import java.util.UUID

import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.directives.{Credentials, DebuggingDirectives}
import authentikat.jwt.{JsonWebToken, JwtHeader}
import com.isv.img_search_api.Errors.CommonError.{ApplicationError, InternalServerError}
import com.isv.img_search_api.Exceptions.ApplicationException
import com.isv.img_search_api.Model.User
import com.isv.img_search_api.db._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.util.{Failure, Success}

trait AuthRoute { self: JwtParser with UserRepo =>

  val api: Api

  val tokenExpiryPeriodInDays: Int
  val header: JwtHeader

  class Api extends Directives with FailFastCirceSupport with JwtParser {

    def route = {
      DebuggingDirectives.logRequestResult("Client ReST", Logging.InfoLevel)(
        echo ~ auth ~ logIn
      )
    }

    private def echo = pathSingleSlash {
      get {
        authenticated { token =>
          complete(s"Hello, $token")
        }
      }
    }

    private def auth =
      path("auth") {
        post {
          entity(as[LoginRequest]) {
            case LoginRequest(email, password) =>
              val token = genUUID
              onComplete(userRepo.addNewUser(User(token, password, email))) {
                case Success(1) =>
                  val claims = setClaims(token, tokenExpiryPeriodInDays)
                  respondWithHeader(RawHeader("Access-Token", JsonWebToken(header, claims, secretKey))) {
                    complete(StatusCodes.OK)
                  }
                case Success(_) | Failure(ApplicationException(ApplicationError(DuplicateEmailError), _)) =>
                  complete(ApplicationError(ErrorDuringRegistration.EmailAlreadyExists))
                case Failure(error) => complete(InternalServerError(error.getMessage))
              }

            case LoginRequest(_, _) => complete(StatusCodes.Unauthorized)
          }
        }
      }

    private def logIn = path("login"){
      post{
        entity(as[LoginRequest]) {
          case LoginRequest(email, password) =>
            onComplete(userRepo.findByEmail(email)) {
              case Success(Some(user)) if user.password == password =>
                val claims = setClaims(email, tokenExpiryPeriodInDays)

                respondWithHeader(RawHeader("Access-Token", JsonWebToken(header, claims, secretKey))) {
                  complete(StatusCodes.OK)
                }
              case Success(_) =>
                complete(StatusCodes.Forbidden -> "error.incorrect-email-or-password")
              case Failure(error) => complete(InternalServerError(error.getMessage))
            }

          case LoginRequest(_, _) => complete(StatusCodes.Unauthorized)
        }
      }
    }

  }

}
