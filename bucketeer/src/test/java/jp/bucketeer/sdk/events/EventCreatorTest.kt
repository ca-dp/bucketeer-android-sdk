package jp.bucketeer.sdk.events

import bucketeer.event.client.EventOuterClass
import bucketeer.feature.ReasonOuterClass
import com.google.protobuf.Any
import com.google.protobuf.Duration
import jp.bucketeer.sdk.evaluation1
import jp.bucketeer.sdk.user1
import org.amshove.kluent.shouldBeGreaterOrEqualTo
import org.amshove.kluent.shouldEqual
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EventCreatorTest {

  @Test fun generateEvaluationEvent() {
    val event = generateEvaluationEvent(1234, evaluation1, user1)

    event.run {
      timestamp shouldEqual 1234
      featureId shouldEqual "test-feature-1"
      featureVersion shouldEqual 9
      userId shouldEqual "user id 1"
      variationId shouldEqual "test-feature-1-variation-A"
      user shouldEqual user1
      reason shouldEqual ReasonOuterClass.Reason.newBuilder().setType(
          ReasonOuterClass.Reason.Type.DEFAULT).build()
    }
  }

  @Test fun generateGoalEvent() {
    val event = generateGoalEvent(1234, "goalId", 100.0, user1, listOf(evaluation1))

    event.run {
      timestamp shouldEqual 1234
      goalId shouldEqual "goalId"
      userId shouldEqual "user id 1"
      value shouldEqual 100.0
      user shouldEqual user1
      evaluationsList shouldEqual listOf(evaluation1)
    }
  }

  @Test fun generateGetEvaluationLatencyMetricsEvent() {
    val time = System.currentTimeMillis() / 1000
    val event = generateGetEvaluationLatencyMetricsEvent(1234,
        mapOf("tag" to "android", "state" to "FULL"))

    event.run {
      timestamp shouldBeGreaterOrEqualTo time
    }

    val getEvaluationLatencyMetricsEvent = event.event.unpackToGetEvaluationLatencyMetricsEvent()
    getEvaluationLatencyMetricsEvent.run {
      labelsMap shouldEqual mapOf("tag" to "android", "state" to "FULL")
      duration shouldEqual Duration.newBuilder().setSeconds(1).setNanos(234000000).build()
    }
  }

  private fun Any.unpackToGetEvaluationLatencyMetricsEvent(): EventOuterClass.GetEvaluationLatencyMetricsEvent {
    return EventOuterClass.GetEvaluationLatencyMetricsEvent.parseFrom(this.value)
  }

  @Test fun generateGetEvaluationSizeMetricsEvent() {
    val time = System.currentTimeMillis() / 1000
    val event = generateGetEvaluationSizeMetricsEvent(1234,
        mapOf("tag" to "android", "state" to "FULL"))

    event.run {
      timestamp shouldBeGreaterOrEqualTo time
    }

    val getEvaluationSizeMetricsEvent = event.event.unpackToGetEvaluationSizeMetricsEvent()
    getEvaluationSizeMetricsEvent.run {
      labelsMap shouldEqual mapOf("tag" to "android", "state" to "FULL")
      sizeByte shouldEqual 1234
    }
  }

  private fun Any.unpackToGetEvaluationSizeMetricsEvent(): EventOuterClass.GetEvaluationSizeMetricsEvent {
    return EventOuterClass.GetEvaluationSizeMetricsEvent.parseFrom(this.value)
  }

  @Test fun generateTimeoutErrorCountMetricsEvent() {
    val time = System.currentTimeMillis() / 1000
    val event = generateTimeoutErrorCountMetricsEvent("tag")

    event.run {
      timestamp shouldBeGreaterOrEqualTo time
    }

    val timeoutErrorCountMetricsEvent = event.event.unpackToTimeoutErrorCountMetricsEvent()
    timeoutErrorCountMetricsEvent.run {
      tag shouldEqual "tag"
    }
  }

  private fun Any.unpackToTimeoutErrorCountMetricsEvent(): EventOuterClass.TimeoutErrorCountMetricsEvent {
    return EventOuterClass.TimeoutErrorCountMetricsEvent.parseFrom(this.value)
  }

  @Test fun generateInternalErrorCountMetricsEvent() {
    val time = System.currentTimeMillis() / 1000
    val event = generateInternalErrorCountMetricsEvent("tag")

    event.run {
      timestamp shouldBeGreaterOrEqualTo time
    }

    val internalErrorCountMetricsEvent = event.event.unpackToInternalErrorCountMetricsEvent()
    internalErrorCountMetricsEvent.run {
      tag shouldEqual "tag"
    }
  }

  private fun Any.unpackToInternalErrorCountMetricsEvent(): EventOuterClass.InternalErrorCountMetricsEvent {
    return EventOuterClass.InternalErrorCountMetricsEvent.parseFrom(this.value)
  }
}
