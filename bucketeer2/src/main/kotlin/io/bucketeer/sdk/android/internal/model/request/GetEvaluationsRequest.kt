package io.bucketeer.sdk.android.internal.model.request

import com.squareup.moshi.JsonClass
import io.bucketeer.sdk.android.internal.model.SourceID
import io.bucketeer.sdk.android.internal.model.User

@JsonClass(generateAdapter = true)
data class GetEvaluationsRequest(
  val tag: String,
  val user: User,
  val source_id: SourceID = SourceID.ANDROID
)
