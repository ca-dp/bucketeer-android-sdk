package jp.bucketeer.sdk

import androidx.annotation.VisibleForTesting

data class BucketeerConfig @VisibleForTesting internal constructor(
  val logSendingIntervalMillis: Long,
  val logSendingMaxBatchQueueCount: Int,
  val pollingEvaluationIntervalMillis: Long
) {
  class Builder {
    private var logSendingIntervalMillis: Long = 60_000
    private var logSendingMaxBatchQueueCount: Int = 50
    private var pollingEvaluationIntervalMillis: Long = 600_000

    fun logSendingIntervalMillis(logSendingInterval: Long): Builder {
      require(logSendingInterval > 0)
      this.logSendingIntervalMillis = logSendingInterval
      return this
    }

    fun logSendingMaxBatchQueueCount(logSendingMaxBatchQueueCount: Int): Builder {
      require(logSendingMaxBatchQueueCount > 0)
      this.logSendingMaxBatchQueueCount = logSendingMaxBatchQueueCount
      return this
    }

    fun pollingEvaluationIntervalMillis(pollingEvaluationIntervalMillis: Long): Builder {
      require(pollingEvaluationIntervalMillis > 0)
      this.pollingEvaluationIntervalMillis = pollingEvaluationIntervalMillis
      return this
    }

    fun build(): BucketeerConfig {
      return BucketeerConfig(
        logSendingIntervalMillis,
        logSendingMaxBatchQueueCount,
        pollingEvaluationIntervalMillis
      )
    }
  }

  companion object {
    internal val DEFAULT: BucketeerConfig = Builder().build()
  }
}
