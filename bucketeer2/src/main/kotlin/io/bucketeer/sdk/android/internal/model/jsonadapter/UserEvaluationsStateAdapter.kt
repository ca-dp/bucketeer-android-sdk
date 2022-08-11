package io.bucketeer.sdk.android.internal.model.jsonadapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import io.bucketeer.sdk.android.internal.model.UserEvaluationsState

class UserEvaluationsStateAdapter {
  @ToJson
  fun toJson(type: UserEvaluationsState): Int = type.value

  @FromJson
  fun fromJson(value: Int): UserEvaluationsState = UserEvaluationsState.from(value)
}
