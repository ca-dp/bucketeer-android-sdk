package io.bucketeer.sdk.android.internal.scheduler

import io.bucketeer.sdk.android.BKTClientImpl
import io.bucketeer.sdk.android.internal.di.Component
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

internal class EvaluationForegroundTask(
  private val component: Component,
  private val executor: ScheduledExecutorService,
) : ScheduledTask {

  override var isStarted: Boolean = false
    private set

  private var scheduledFuture: ScheduledFuture<*>? = null

  internal fun reschedule() {
    scheduledFuture?.cancel(false)
    scheduledFuture = executor.scheduleWithFixedDelay(
      { BKTClientImpl.fetchEvaluationsSync(component, executor, null) },
      component.config.pollingInterval,
      component.config.pollingInterval,
      TimeUnit.MILLISECONDS,
    )
  }

  override fun start() {
    isStarted = true
    reschedule()
  }

  override fun stop() {
    isStarted = false
    scheduledFuture?.cancel(false)
  }
}
