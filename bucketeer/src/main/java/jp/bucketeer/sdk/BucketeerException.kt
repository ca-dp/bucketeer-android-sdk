package jp.bucketeer.sdk

import io.grpc.Status
import io.grpc.StatusRuntimeException

sealed class BucketeerException(
    message: String,
    cause: Throwable? = null
) : Exception(
    message,
    cause
) {
  class ApiCancelException(cause: Throwable?) : BucketeerException(
      "The operation was cancelled.",
      cause
  )

  class ApiUnknownException(cause: Throwable?) : BucketeerException(
      "Unknown error.",
      cause
  )

  class ApiInvocationFailException(message: String, cause: Throwable?) : BucketeerException(
      message,
      cause
  )

  class ApiServerException(cause: Throwable?) : BucketeerException(
      "The server is currently error.",
      cause
  )

  class ApiUnavailableException(cause: Throwable?) : BucketeerException(
      "The service is currently unavailable.",
      cause
  )

  class ApiDataLossException(cause: Throwable?) : BucketeerException(
      "Unrecoverable data loss or corruption.",
      cause
  )

  class ApiUnauthenticatedException(cause: Throwable?) : BucketeerException(
      "Does not have valid authentication credentials.",
      cause
  )

  class UnknownException(cause: Throwable?) : BucketeerException(
      "Unknown error",
      cause
  )

  class IllegalArgumentException(message: String) : BucketeerException(
      message
  )

  class IllegalStateException(message: String) : BucketeerException(
      message
  )
}

internal fun Throwable.toBucketeerException(): BucketeerException = when (this) {
  is StatusRuntimeException -> when (status.code) {
    Status.Code.CANCELLED -> BucketeerException.ApiCancelException(this)
    Status.Code.UNKNOWN -> BucketeerException.ApiUnknownException(this)
    Status.Code.INVALID_ARGUMENT -> BucketeerException.ApiInvocationFailException(
        "Client specified an invalid argument.",
        this
    )
    Status.Code.DEADLINE_EXCEEDED -> BucketeerException.ApiInvocationFailException(
        "Deadline expired before operation could complete.",
        this
    )
    Status.Code.NOT_FOUND -> BucketeerException.ApiInvocationFailException(
        "Some requested entity was not found.",
        this
    )
    Status.Code.ALREADY_EXISTS -> BucketeerException.ApiInvocationFailException(
        "Some entity that we attempted to create already exists.",
        this
    )
    Status.Code.PERMISSION_DENIED -> BucketeerException.ApiInvocationFailException(
        "The caller does not have permission to execute the specified operation.",
        this
    )
    Status.Code.RESOURCE_EXHAUSTED -> BucketeerException.ApiInvocationFailException(
        "Some resource has been exhausted.",
        this
    )
    Status.Code.FAILED_PRECONDITION -> BucketeerException.ApiInvocationFailException(
        "Operation was rejected because the system is not in a state required for the operation's execution.",
        this
    )
    Status.Code.ABORTED -> BucketeerException.ApiInvocationFailException(
        "The operation was aborted.", this)
    Status.Code.OUT_OF_RANGE -> BucketeerException.ApiInvocationFailException(
        "The operation was attempted past the valid range.",
        this
    )
    Status.Code.UNIMPLEMENTED -> BucketeerException.ApiInvocationFailException(
        "The operation is not implemented or not supported/enabled in this service.",
        this
    )
    Status.Code.INTERNAL -> BucketeerException.ApiServerException(this)
    Status.Code.UNAVAILABLE -> BucketeerException.ApiUnavailableException(this)
    Status.Code.DATA_LOSS -> BucketeerException.ApiDataLossException(this)
    Status.Code.UNAUTHENTICATED -> BucketeerException.ApiUnauthenticatedException(this)
    else -> BucketeerException.UnknownException(this)
  }
  else -> BucketeerException.UnknownException(this)
}


