package io.bucketeer.sdk.android

sealed class BKTException(
  message: String,
  cause: Throwable? = null
) : Exception(
  message,
  cause
) {
  class ApiCancelException(cause: Throwable?) : BKTException(
    "The operation was cancelled.",
    cause
  )

  class ApiUnknownException(cause: Throwable?) : BKTException(
    "Unknown error.",
    cause
  )

  class ApiInvocationFailException(message: String, cause: Throwable?) : BKTException(
    message,
    cause
  )

  class ApiServerException(cause: Throwable?) : BKTException(
    "The server is currently error.",
    cause
  )

  class ApiUnavailableException(cause: Throwable?) : BKTException(
    "The service is currently unavailable.",
    cause
  )

  class ApiDataLossException(cause: Throwable?) : BKTException(
    "Unrecoverable data loss or corruption.",
    cause
  )

  class ApiUnauthenticatedException(cause: Throwable?) : BKTException(
    "Does not have valid authentication credentials.",
    cause
  )

  class UnknownException(cause: Throwable?) : BKTException(
    "Unknown error",
    cause
  )

  class IllegalArgumentException(message: String) : BKTException(
    message
  )

  class IllegalStateException(message: String) : BKTException(
    message
  )
}
