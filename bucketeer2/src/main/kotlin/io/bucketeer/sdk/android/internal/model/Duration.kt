package io.bucketeer.sdk.android.internal.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Duration(
  val seconds: Long,
  val nanos: Int
)
