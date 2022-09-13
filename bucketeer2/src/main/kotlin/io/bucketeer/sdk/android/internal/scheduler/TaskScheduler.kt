package io.bucketeer.sdk.android.internal.scheduler

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.bucketeer.sdk.android.internal.di.Component
import java.util.concurrent.ScheduledExecutorService

internal class TaskScheduler(
  component: Component,
  executor: ScheduledExecutorService,
) : DefaultLifecycleObserver {

  private val foregroundSchedulers: List<ScheduledTask> = listOf(
    EvaluationForegroundTask(component, executor),
    EventForegroundTask(component, executor),
  )

  // app start or back to foreground
  override fun onStart(owner: LifecycleOwner) {
    foregroundSchedulers.forEach { it.start() }

    // TODO: stop background tasks
  }

  // to background
  override fun onStop(owner: LifecycleOwner) {
    foregroundSchedulers.forEach { it.stop() }

    // TODO: start background tasks
  }
}
