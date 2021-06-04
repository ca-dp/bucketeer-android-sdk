package jp.bucketeer.sdk

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class BucketeerConfigTest {
  @Test fun build() {
    BucketeerConfig.Builder()
        .logSendingIntervalMillis(1)
        .logSendingMaxBatchQueueCount(2)
        .pollingEvaluationIntervalMillis(3)
        .build() shouldBeEqualTo BucketeerConfig(
        logSendingIntervalMillis = 1,
        logSendingMaxBatchQueueCount = 2,
        pollingEvaluationIntervalMillis = 3
    )
  }

  @Test(expected = IllegalArgumentException::class)
  fun logSendIntervalInMills_error() {
    BucketeerConfig.Builder().logSendingIntervalMillis(-1)
  }

  @Test(expected = IllegalArgumentException::class)
  fun logSendingMaxBatchQueueCount_error() {
    BucketeerConfig.Builder().logSendingMaxBatchQueueCount(-1)
  }

  @Test(expected = IllegalArgumentException::class)
  fun pollingEvaluationIntervalMillis_error() {
    BucketeerConfig.Builder().pollingEvaluationIntervalMillis(-1)
  }
}
