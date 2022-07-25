package jp.bucketeer.sdk

import io.grpc.Status
import io.grpc.StatusRuntimeException

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

internal fun Throwable.toBKTException(): BKTException = when (this) {
  is StatusRuntimeException -> when (status.code) {
    Status.Code.CANCELLED -> BKTException.ApiCancelException(this)
    Status.Code.UNKNOWN -> BKTException.ApiUnknownException(this)
    Status.Code.INVALID_ARGUMENT -> BKTException.ApiInvocationFailException(
      "Client specified an invalid argument.",
      this
    )
    Status.Code.DEADLINE_EXCEEDED -> BKTException.ApiInvocationFailException(
      "Deadline expired before operation could complete.",
      this
    )
    Status.Code.NOT_FOUND -> BKTException.ApiInvocationFailException(
      "Some requested entity was not found.",
      this
    )
    Status.Code.ALREADY_EXISTS -> BKTException.ApiInvocationFailException(
      "Some entity that we attempted to create already exists.",
      this
    )
    Status.Code.PERMISSION_DENIED -> BKTException.ApiInvocationFailException(
      "The caller does not have permission to execute the specified operation.",
      this
    )
    Status.Code.RESOURCE_EXHAUSTED -> BKTException.ApiInvocationFailException(
      "Some resource has been exhausted.",
      this
    )
    Status.Code.FAILED_PRECONDITION -> BKTException.ApiInvocationFailException(
      "Operation was rejected because the system is not in a state required for the operation's execution.",
      this
    )
    Status.Code.ABORTED -> BKTException.ApiInvocationFailException(
      "The operation was aborted.", this
    )
    Status.Code.OUT_OF_RANGE -> BKTException.ApiInvocationFailException(
      "The operation was attempted past the valid range.",
      this
    )
    Status.Code.UNIMPLEMENTED -> BKTException.ApiInvocationFailException(
      "The operation is not implemented or not supported/enabled in this service.",
      this
    )
    Status.Code.INTERNAL -> BKTException.ApiServerException(this)
    Status.Code.UNAVAILABLE -> BKTException.ApiUnavailableException(this)
    Status.Code.DATA_LOSS -> BKTException.ApiDataLossException(this)
    Status.Code.UNAUTHENTICATED -> BKTException.ApiUnauthenticatedException(this)
    else -> BKTException.UnknownException(this)
  }
  else -> BKTException.UnknownException(this)
}
