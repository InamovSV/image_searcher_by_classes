package com.isv.img_search_api

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec

object Errors {
  sealed trait CommonError

  object CommonError {
    implicit val configuration: Configuration = Configuration.default.withDiscriminator("type")
    implicit val codec: Codec[CommonError] = deriveConfiguredCodec[CommonError]

    case class ApplicationError(error: String) extends CommonError

    object ApplicationError {
      implicit val applicationErrorCodec: Codec[ApplicationError] = deriveCodec[ApplicationError]
    }

    case class NotFoundError(error: String) extends CommonError

    object NotFoundError {
      implicit val notFoundErrorCodec: Codec[NotFoundError] = deriveCodec[NotFoundError]
    }

    case class InternalServerError(error: String) extends CommonError

    object InternalServerError {
      implicit val internalServerErrorCodec: Codec[InternalServerError] = deriveCodec[InternalServerError]
    }

    case class ModelFieldValidationError(error: String, error_description: Option[String] = None)

    object ModelFieldValidationError {
      implicit val modelFieldValidationErrorCodec: Codec[ModelFieldValidationError] = deriveCodec[ModelFieldValidationError]
    }

    case class ModelValidationError(error: String, fields_errors: Map[String, ModelFieldValidationError] = Map.empty) extends CommonError

    object ModelValidationError {
      implicit val modelValidationErrorCodec: Codec[ModelValidationError] = deriveCodec[ModelValidationError]
    }

    final val InternalError = "error.internal"
    final val ValidationError = "error.validation"
    final val EntityNotFoundError = "error.entity-not-found"
    final val NonUniqueResultError = "error.non-unique-result"
  }

}
