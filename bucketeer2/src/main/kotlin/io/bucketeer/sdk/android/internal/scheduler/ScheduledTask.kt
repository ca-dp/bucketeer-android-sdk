package io.bucketeer.sdk.android.internal.scheduler

internal interface ScheduledTask {
  val isStarted: Boolean
  fun start()
  fun stop()
}
