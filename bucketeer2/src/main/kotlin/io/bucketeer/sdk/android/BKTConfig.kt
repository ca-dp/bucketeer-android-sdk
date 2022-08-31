package io.bucketeer.sdk.android

import io.bucketeer.sdk.android.internal.logw
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

internal const val DEFAULT_FLUSH_INTERVAL_MILLIS: Long = 30_000 // 30 seconds
internal const val DEFAULT_MAX_QUEUE_SIZE: Int = 50
internal const val DEFAULT_POLLING_INTERVAL_MILLIS: Long = 600_000 // 10 minutes
internal const val DEFAULT_BACKGROUND_POLLING_INTERVAL_MILLIS: Long = 3_600_000 // 1 hour
internal const val MIN_POLLING_INTERVAL_MILLIS: Long = 300_000 // 5 minutes
internal const val MIN_BACKGROUND_POLLING_INTERVAL_MILLIS: Long = 1_200_000 // 20 minutes

data class BKTConfig internal constructor(
  val apiKey: String,
  val endpoint: String,
  val featureTag: String,
  val eventsFlushInterval: Long,
  val eventsMaxBatchQueueCount: Int,
  val pollingInterval: Long,
  val backgroundPollingInterval: Long,
  val debugMode: Boolean
) {

  companion object {
    fun builder(): Builder = Builder()
  }

  class Builder internal constructor() {
    private var apiKey: String? = null
    private var endpoint: String? = null
    private var featureTag: String? = null
    private var eventsFlushInterval: Long = DEFAULT_FLUSH_INTERVAL_MILLIS
    private var eventsMaxQueueSize: Int = DEFAULT_MAX_QUEUE_SIZE
    private var pollingInterval: Long = DEFAULT_POLLING_INTERVAL_MILLIS
    private var backgroundPollingInterval: Long = DEFAULT_BACKGROUND_POLLING_INTERVAL_MILLIS
    private var debugMode: Boolean = false

    fun apiKey(apiKey: String): Builder {
      this.apiKey = apiKey
      return this
    }

    fun endpoint(endpoint: String): Builder {
      this.endpoint = endpoint
      return this
    }

    fun featureTag(featureTag: String): Builder {
      this.featureTag = featureTag
      return this
    }

    fun eventsFlushInterval(intervalMillis: Long): Builder {
      this.eventsFlushInterval = intervalMillis
      return this
    }

    fun eventsMaxQueueSize(maxQueueSize: Int): Builder {
      this.eventsMaxQueueSize = maxQueueSize
      return this
    }

    fun pollingInterval(intervalMillis: Long): Builder {
      this.pollingInterval = intervalMillis
      return this
    }

    fun backgroundPollingInterval(intervalMillis: Long): Builder {
      this.backgroundPollingInterval = intervalMillis
      return this
    }

    fun debugMode(debugMode: Boolean): Builder {
      this.debugMode = debugMode
      return this
    }

    fun build(): BKTConfig {
      require(!apiKey.isNullOrEmpty()) { "apiKey is required" }
      require(endpoint?.toHttpUrlOrNull() != null) { "endpoint is invalid" }
      require(!featureTag.isNullOrEmpty()) { "featureTag is required" }

      if (pollingInterval < MIN_POLLING_INTERVAL_MILLIS) {
        logw {
          "pollingInterval: $pollingInterval was set below the minimum allowed: " +
              "$MIN_POLLING_INTERVAL_MILLIS. It will use the minimum value."
        }
        pollingInterval = MIN_POLLING_INTERVAL_MILLIS
      }

      if (backgroundPollingInterval < MIN_BACKGROUND_POLLING_INTERVAL_MILLIS) {
        logw {
          "backgroundPollingInterval: $backgroundPollingInterval was set below the minimum allowed: " +
              "$MIN_BACKGROUND_POLLING_INTERVAL_MILLIS. It will use the minimum value."
        }
        backgroundPollingInterval = MIN_BACKGROUND_POLLING_INTERVAL_MILLIS
      }

      return BKTConfig(
        apiKey = apiKey!!,
        endpoint = endpoint!!,
        featureTag = featureTag!!,
        eventsFlushInterval = this.eventsFlushInterval,
        eventsMaxBatchQueueCount = this.eventsMaxQueueSize,
        pollingInterval = this.pollingInterval,
        backgroundPollingInterval = this.backgroundPollingInterval,
        debugMode = this.debugMode
      )
    }
  }
}
