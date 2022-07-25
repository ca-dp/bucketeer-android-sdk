package jp.bucketeer.sdk

import android.util.Patterns
import jp.bucketeer.sdk.log.logw
import java.util.regex.Matcher

private const val DEFAULT_FLUSH_INTERVAL_MILLIS: Long = 30_000 // 30 seconds
private const val DEFAULT_MAX_QUEUE_SIZE: Int = 50
private const val DEFAULT_POLLING_INTERVAL_MILLIS: Long = 600_000 // 10 minutes
private const val DEFAULT_BACKGROUND_POLLING_INTERVAL_MILLIS: Long = 3_600_000 // 1 hour
private const val MIN_POLLING_INTERVAL_MILLIS: Long = 300_000 // 5 minutes
private const val MIN_BACKGROUND_POLLING_INTERVAL_MILLIS: Long = 1_200_000 // 20 minutes

data class BKTConfig private constructor(
  val featureTag: String,
  var apiKey: String,
  val endpoint: String,
  val eventsFlushInterval: Long,
  val eventsMaxBatchQueueCount: Int,
  val pollingInterval: Long,
  val backgroundPollingInterval: Long
) {

  class Builder {
    private lateinit var apiKey: String
    private lateinit var endpoint: String
    private lateinit var featureTag: String
    private var eventsFlushInterval: Long = DEFAULT_FLUSH_INTERVAL_MILLIS
    private var eventsMaxQueueSize: Int = DEFAULT_MAX_QUEUE_SIZE
    private var pollingInterval: Long = DEFAULT_POLLING_INTERVAL_MILLIS
    private var backgroundPollingInterval: Long = DEFAULT_BACKGROUND_POLLING_INTERVAL_MILLIS
    private var debugMode: Boolean = false

    fun apiKey(apiKey: String): Builder {
      this.apiKey = apiKey
      return this
    }

    fun apiURL(apiURL: String): Builder {
      this.endpoint = endpoint
      return this
    }

    fun featureTag(featureTag: String): Builder {
      this.featureTag = featureTag
      return this
    }

    fun eventsFlushInterval(interval: Long): Builder {
      this.eventsFlushInterval = interval
      return this
    }

    fun eventsMaxQueueSize(maxQueueSize: Int): Builder {
      this.eventsMaxQueueSize = maxQueueSize
      return this
    }

    fun pollingInterval(pollingInterval: Long): Builder {
      this.pollingInterval = pollingInterval
      return this
    }

    fun backgroundPollingInterval(backgroundPollingInterval: Long): Builder {
      this.backgroundPollingInterval = backgroundPollingInterval
      return this
    }

    fun debugMode(debugMode: Boolean): Builder {
      this.debugMode = debugMode
      return this
    }

    fun build(): BKTConfig {
      if (apiKey.isEmpty()) {
        throw BKTException.IllegalArgumentException(
          "The api key is required."
        )
      }

      if (endpoint.isEmpty()) {
        throw BKTException.IllegalArgumentException(
          "The api url is required."
        )
      }

      val m: Matcher = Patterns.WEB_URL.matcher(endpoint)
      if (!m.matches()) {
        throw BKTException.IllegalArgumentException(
          "The api url is invalid."
        )
      }

      if (featureTag.isEmpty()) {
        throw BKTException.IllegalArgumentException(
          "The feature tag is required."
        )
      }

      if (pollingInterval < MIN_POLLING_INTERVAL_MILLIS) {
        logw {
          "The pollingInterval: $pollingInterval was set below the minimum allowed: " +
              "$MIN_POLLING_INTERVAL_MILLIS. It will use the minimum value."
        }
        pollingInterval = MIN_POLLING_INTERVAL_MILLIS
      }

      if (backgroundPollingInterval < MIN_BACKGROUND_POLLING_INTERVAL_MILLIS) {
        logw {
          "The backgroundPollingInterval: $backgroundPollingInterval was set below the minimum allowed: " +
              "$MIN_BACKGROUND_POLLING_INTERVAL_MILLIS. It will use the minimum value."
        }
        backgroundPollingInterval = MIN_BACKGROUND_POLLING_INTERVAL_MILLIS
      }

      return BKTConfig(
        featureTag,
        apiKey,
        endpoint,
        eventsFlushInterval,
        eventsMaxQueueSize,
        pollingInterval,
        backgroundPollingInterval
      )
    }
  }
}
