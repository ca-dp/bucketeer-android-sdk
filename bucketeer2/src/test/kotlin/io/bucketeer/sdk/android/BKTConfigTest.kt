package io.bucketeer.sdk.android

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class BKTConfigTest {
  @Test
  fun build() {
    val actual = BKTConfig.builder()
      .apiKey("api-key")
      .endpoint("https://example.com")
      .featureTag("feature-tag")
      .build()

    assertThat(actual).isEqualTo(
      BKTConfig(
        apiKey = "api-key",
        endpoint = "https://example.com",
        featureTag = "feature-tag",
        eventsFlushInterval = DEFAULT_FLUSH_INTERVAL_MILLIS,
        eventsMaxBatchQueueCount = DEFAULT_MAX_QUEUE_SIZE,
        pollingInterval = DEFAULT_POLLING_INTERVAL_MILLIS,
        backgroundPollingInterval = DEFAULT_BACKGROUND_POLLING_INTERVAL_MILLIS,
        debugMode = false
      )
    )
  }

  @Test
  fun `apiKey - unset`() {
    val error = assertThrows(BKTException.IllegalArgumentException::class.java) {
      BKTConfig.builder()
        .endpoint("https://example.com")
        .featureTag("feature-tag")
        .build()
    }

    assertThat(error).hasMessageThat().isEqualTo("apiKey is required")
  }

  @Test
  fun `apiKey - empty`() {
    val error = assertThrows(BKTException.IllegalArgumentException::class.java) {
      BKTConfig.builder()
        .apiKey("")
        .endpoint("https://example.com")
        .featureTag("feature-tag")
        .build()
    }

    assertThat(error).hasMessageThat().isEqualTo("apiKey is required")
  }

  @Test
  fun `endpoint - unset`() {
    val error = assertThrows(BKTException.IllegalArgumentException::class.java) {
      BKTConfig.builder()
        .apiKey("api-key")
        .featureTag("feature-tag")
        .build()
    }

    assertThat(error).hasMessageThat().isEqualTo("endpoint is invalid")
  }

  @Test
  fun `endpoint - invalid`() {
    val error = assertThrows(BKTException.IllegalArgumentException::class.java) {
      BKTConfig.builder()
        .apiKey("api-key")
        .endpoint("some invalid value")
        .featureTag("feature-tag")
        .build()
    }

    assertThat(error).hasMessageThat().isEqualTo("endpoint is invalid")
  }

  @Test
  fun `featureTag - unset`() {
    val error = assertThrows(BKTException.IllegalArgumentException::class.java) {
      BKTConfig.builder()
        .apiKey("api-key")
        .endpoint("https://example.com")
        .build()
    }

    assertThat(error).hasMessageThat().isEqualTo("featureTag is required")
  }

  @Test
  fun `featureTag - empty`() {
    val error = assertThrows(BKTException.IllegalArgumentException::class.java) {
      BKTConfig.builder()
        .apiKey("api-key")
        .endpoint("https://example.com")
        .featureTag("")
        .build()
    }

    assertThat(error).hasMessageThat().isEqualTo("featureTag is required")
  }

  @Test
  fun `pollingInterval - force set min value`() {
    val actual = BKTConfig.builder()
      .apiKey("api-key")
      .endpoint("https://example.com")
      .featureTag("feature-tag")
      .pollingInterval(MIN_POLLING_INTERVAL_MILLIS - 100)
      .build()

    assertThat(actual).isEqualTo(
      BKTConfig(
        apiKey = "api-key",
        endpoint = "https://example.com",
        featureTag = "feature-tag",
        eventsFlushInterval = DEFAULT_FLUSH_INTERVAL_MILLIS,
        eventsMaxBatchQueueCount = DEFAULT_MAX_QUEUE_SIZE,
        pollingInterval = MIN_POLLING_INTERVAL_MILLIS,
        backgroundPollingInterval = DEFAULT_BACKGROUND_POLLING_INTERVAL_MILLIS,
        debugMode = false
      )
    )
  }

  @Test
  fun `backgroundPollingInterval - force set min value`() {
    val actual = BKTConfig.builder()
      .apiKey("api-key")
      .endpoint("https://example.com")
      .featureTag("feature-tag")
      .backgroundPollingInterval(MIN_BACKGROUND_POLLING_INTERVAL_MILLIS - 100)
      .build()

    assertThat(actual).isEqualTo(
      BKTConfig(
        apiKey = "api-key",
        endpoint = "https://example.com",
        featureTag = "feature-tag",
        eventsFlushInterval = DEFAULT_FLUSH_INTERVAL_MILLIS,
        eventsMaxBatchQueueCount = DEFAULT_MAX_QUEUE_SIZE,
        pollingInterval = DEFAULT_POLLING_INTERVAL_MILLIS,
        backgroundPollingInterval = MIN_BACKGROUND_POLLING_INTERVAL_MILLIS,
        debugMode = false
      )
    )
  }
}
