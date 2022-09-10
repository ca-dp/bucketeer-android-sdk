package jp.bucketeer.sdk.events

import bucketeer.event.client.EventOuterClass
import bucketeer.feature.ReasonOuterClass
import com.google.protobuf.Any
import com.google.protobuf.Duration
import jp.bucketeer.sdk.evaluation1
import jp.bucketeer.sdk.user1
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeGreaterOrEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EventCreatorTest {

  @Test
  fun generateEvaluationEvent() {
    val featureTag = "feature-tag"
    val event = generateEvaluationEvent(featureTag, 1234, evaluation1, user1)

    event.run {
      tag shouldBeEqualTo featureTag
      sourceId shouldBeEqualTo EventOuterClass.SourceId.ANDROID
      timestamp shouldBeEqualTo 1234
      featureId shouldBeEqualTo "test-feature-1"
      featureVersion shouldBeEqualTo 9
      userId shouldBeEqualTo "user id 1"
      variationId shouldBeEqualTo "test-feature-1-variation-A"
      user shouldBeEqualTo user1
      reason shouldBeEqualTo ReasonOuterClass.Reason.newBuilder().setType(
        ReasonOuterClass.Reason.Type.DEFAULT,
      ).build()
    }
  }

  @Test
  fun generateDefaultEvaluationEvent() {
    val featureTag = "feature-tag"
    val featureId = "feature-id"
    val event = generateDefaultEvaluationEvent(featureTag, 1234, user1, featureId)

    event.run {
      tag shouldBeEqualTo featureTag
      featureId shouldBeEqualTo featureId
      sourceId shouldBeEqualTo EventOuterClass.SourceId.ANDROID
      timestamp shouldBeEqualTo 1234
      userId shouldBeEqualTo "user id 1"
      user shouldBeEqualTo user1
      reason shouldBeEqualTo ReasonOuterClass.Reason.newBuilder().setType(
        ReasonOuterClass.Reason.Type.CLIENT,
      ).build()
    }
  }

  @Test
  fun generateGoalEvent() {
    val featureTag = "feature-tag"
    val event = generateGoalEvent(featureTag, 1234, "goalId", 100.0, user1, listOf(evaluation1))

    event.run {
      tag shouldBeEqualTo featureTag
      sourceId shouldBeEqualTo EventOuterClass.SourceId.ANDROID
      timestamp shouldBeEqualTo 1234
      goalId shouldBeEqualTo "goalId"
      userId shouldBeEqualTo "user id 1"
      value shouldBeEqualTo 100.0
      user shouldBeEqualTo user1
      evaluationsList shouldBeEqualTo listOf(evaluation1)
    }
  }

  @Test
  fun generateGetEvaluationLatencyMetricsEvent() {
    val time = System.currentTimeMillis() / 1000
    val event = generateGetEvaluationLatencyMetricsEvent(
      1234,
      mapOf("tag" to "android", "state" to "FULL"),
    )

    event.run {
      timestamp shouldBeGreaterOrEqualTo time
    }

    val getEvaluationLatencyMetricsEvent = event.event.unpackToGetEvaluationLatencyMetricsEvent()
    getEvaluationLatencyMetricsEvent.run {
      labelsMap shouldBeEqualTo mapOf("tag" to "android", "state" to "FULL")
      duration shouldBeEqualTo Duration.newBuilder().setSeconds(1).setNanos(234000000).build()
    }
  }

  private fun Any.unpackToGetEvaluationLatencyMetricsEvent(): EventOuterClass.GetEvaluationLatencyMetricsEvent {
    return EventOuterClass.GetEvaluationLatencyMetricsEvent.parseFrom(this.value)
  }

  @Test
  fun generateGetEvaluationSizeMetricsEvent() {
    val time = System.currentTimeMillis() / 1000
    val event = generateGetEvaluationSizeMetricsEvent(
      1234,
      mapOf("tag" to "android", "state" to "FULL"),
    )

    event.run {
      timestamp shouldBeGreaterOrEqualTo time
    }

    val getEvaluationSizeMetricsEvent = event.event.unpackToGetEvaluationSizeMetricsEvent()
    getEvaluationSizeMetricsEvent.run {
      labelsMap shouldBeEqualTo mapOf("tag" to "android", "state" to "FULL")
      sizeByte shouldBeEqualTo 1234
    }
  }

  private fun Any.unpackToGetEvaluationSizeMetricsEvent(): EventOuterClass.GetEvaluationSizeMetricsEvent {
    return EventOuterClass.GetEvaluationSizeMetricsEvent.parseFrom(this.value)
  }

  @Test
  fun generateTimeoutErrorCountMetricsEvent() {
    val time = System.currentTimeMillis() / 1000
    val event = generateTimeoutErrorCountMetricsEvent("tag")

    event.run {
      timestamp shouldBeGreaterOrEqualTo time
    }

    val timeoutErrorCountMetricsEvent = event.event.unpackToTimeoutErrorCountMetricsEvent()
    timeoutErrorCountMetricsEvent.run {
      tag shouldBeEqualTo "tag"
    }
  }

  private fun Any.unpackToTimeoutErrorCountMetricsEvent(): EventOuterClass.TimeoutErrorCountMetricsEvent {
    return EventOuterClass.TimeoutErrorCountMetricsEvent.parseFrom(this.value)
  }

  @Test
  fun generateInternalErrorCountMetricsEvent() {
    val time = System.currentTimeMillis() / 1000
    val event = generateInternalErrorCountMetricsEvent("tag")

    event.run {
      timestamp shouldBeGreaterOrEqualTo time
    }

    val internalErrorCountMetricsEvent = event.event.unpackToInternalErrorCountMetricsEvent()
    internalErrorCountMetricsEvent.run {
      tag shouldBeEqualTo "tag"
    }
  }

  private fun Any.unpackToInternalErrorCountMetricsEvent(): EventOuterClass.InternalErrorCountMetricsEvent {
    return EventOuterClass.InternalErrorCountMetricsEvent.parseFrom(this.value)
  }
}
