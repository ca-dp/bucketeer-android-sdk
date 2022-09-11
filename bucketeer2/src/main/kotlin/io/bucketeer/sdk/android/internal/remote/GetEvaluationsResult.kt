package io.bucketeer.sdk.android.internal.remote

import io.bucketeer.sdk.android.BKTException
import io.bucketeer.sdk.android.internal.model.response.GetEvaluationsResponse

sealed class GetEvaluationsResult {
  data class Success(
    val value: GetEvaluationsResponse,
    val millis: Long,
    val sizeByte: Int,
    val featureTag: String,
  ) : GetEvaluationsResult()

  data class Failure(
    val error: BKTException,
    val featureTag: String,
  ) : GetEvaluationsResult()
}
