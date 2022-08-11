package io.bucketeer.sdk.android.internal.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
  val id: String,
  val data: Map<String, String> = emptyMap(),
  val tagged_data: Map<String, UserData> = emptyMap()
)
