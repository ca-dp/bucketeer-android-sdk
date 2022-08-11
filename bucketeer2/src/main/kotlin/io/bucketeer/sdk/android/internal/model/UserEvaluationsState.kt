package io.bucketeer.sdk.android.internal.model

enum class UserEvaluationsState(val value: Int) {
  QUEUED(0),
  PARTIAL(1),
  FULL(2),
  ;

  companion object {
    fun from(value: Int): UserEvaluationsState = values().first { it.value == value }
  }
}
