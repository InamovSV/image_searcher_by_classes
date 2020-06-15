package com.isv.img_search_api.api

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.model.{HttpHeader, StatusCodes}
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.server.{Directive1, Directives}
import akka.http.scaladsl.server.Directives.{failWith, headerValue, provide}
import authentikat.jwt.{JsonWebToken, JwtClaimsSet}
import com.isv.img_search_api.Exceptions.ApplicationException
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import io.circe.parser.decode
import io.circe.syntax._

import scala.concurrent.ExecutionContext

trait JwtParser {

  val ec: ExecutionContext

  val secretKey: String

  trait JwtParser { self: Directives =>
    protected def getClaims(jwt: String) = jwt match {
      case JsonWebToken(_, claims, _) => claims.asSimpleMap.toOption
      case _                          => None
    }

    protected def userToken(jwt: String) = jwt match {
      case JsonWebToken(_, claims, _) => claims.asSimpleMap.toOption.flatMap(_.get("token"))
      case _                          => None
    }

    protected def isTokenExpired(jwt: String) = getClaims(jwt) match {
      case Some(claims) =>
        claims.get("expiredAt") match {
          case Some(value) => value.toLong < System.currentTimeMillis()
          case None        => false
        }
      case None => false
    }

    protected def authenticated: Directive1[String] =
      optionalHeaderValueByName("Authorization").flatMap {
        case Some(jwt) if isTokenExpired(jwt) =>
          complete(StatusCodes.Unauthorized -> "Token expired.")

        case Some(jwt) if JsonWebToken.validate(jwt, secretKey) =>
          userToken(jwt) match {
            case Some(token) => provide(token)
            case None => complete(StatusCodes.BadRequest -> ErrorEmailMissing)
          }

        case _ => complete(StatusCodes.Unauthorized)
      }

    protected def setClaims(email: String, expiryPeriodInDays: Long) = JwtClaimsSet(
      Map("email" -> email,
        "expiredAt" -> (System.currentTimeMillis() + TimeUnit.DAYS
          .toMillis(expiryPeriodInDays)))
    )
  }
}
