package io.bucketeer.sdk.android.internal.model

import com.squareup.moshi.JsonClass

internal val NANOS_PER_MILLISECOND: Long = 1000000
internal val MILLIS_PER_SECOND: Long = 1000

@JsonClass(generateAdapter = true)
data class Duration(
  val seconds: Long,
  val nanos: Int,
)

internal fun Duration(millis: Long): Duration {
  return Duration(
    seconds = millis / MILLIS_PER_SECOND,
    nanos = (millis % MILLIS_PER_SECOND * NANOS_PER_MILLISECOND).toInt(),
  )
}
