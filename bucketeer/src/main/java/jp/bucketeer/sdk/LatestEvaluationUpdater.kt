package jp.bucketeer.sdk

import jp.bucketeer.sdk.evaluation.LatestEvaluationActionCreator
import jp.bucketeer.sdk.user.UserHolder
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

internal class LatestEvaluationUpdater(
    private val logSendingIntervalMillis: Long,
    private val latestEvaluationActionCreator: LatestEvaluationActionCreator,
    private val userHolder: UserHolder,
    private val scheduledExecutorService: ScheduledExecutorService
) : ScheduledTask {
  override var isStarted: Boolean = false
  private var scheduledFuture: ScheduledFuture<*>? = null

  override fun start() {
    isStarted = true
    reschedule()
  }

  private fun reschedule() {
    scheduledFuture?.cancel(false)
    scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(
        { refreshFromApi() },
        logSendingIntervalMillis,
        logSendingIntervalMillis,
        TimeUnit.MILLISECONDS
    )
  }

  override fun stop() {
    isStarted = false
    scheduledFuture?.cancel(false)
  }

  private fun refreshFromApi() {
    latestEvaluationActionCreator.refreshLatestEvaluationFromApi(userHolder.user)
  }
}
