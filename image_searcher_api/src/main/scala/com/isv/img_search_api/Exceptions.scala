package com.isv.img_search_api

import com.isv.img_search_api.Errors.CommonError.ApplicationError

object Exceptions {

  abstract class RootException(appError: ApplicationError, cause: Option[Throwable] = None) extends RuntimeException(appError.error, cause.orNull)

  case class ApplicationException(error: ApplicationError, cause: Option[Throwable] = None) extends RootException(error, cause)

  object ApplicationException {
    def apply(error: String): ApplicationException = new ApplicationException(ApplicationError(error))
    def apply(error: String, cause: Throwable): ApplicationException = new ApplicationException(ApplicationError(error), Option(cause))
  }
}
