package io.bucketeer.sdk.android.internal.model.response

import com.squareup.moshi.JsonClass
import io.bucketeer.sdk.android.internal.model.UserEvaluations
import io.bucketeer.sdk.android.internal.model.UserEvaluationsState

@JsonClass(generateAdapter = true)
data class GetEvaluationsDataResponse(
  val state: UserEvaluationsState,
  val evaluations: UserEvaluations,
  val user_evaluations_id: String
)
