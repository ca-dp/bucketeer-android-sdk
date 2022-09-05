package io.bucketeer.sdk.android.internal.remote

import io.bucketeer.sdk.android.internal.model.UserEvaluationsState
import io.bucketeer.sdk.android.internal.model.response.GetEvaluationsResponse

sealed class GetEvaluationsResult {
  data class Success(
    val value: GetEvaluationsResponse,
    val millis: Long,
    val sizeByte: Int,
    val featureTag: String,
    val state: UserEvaluationsState
  ) : GetEvaluationsResult()

  data class Failure(
    val error: Throwable,
    val featureTag: String
  ) : GetEvaluationsResult()
}
