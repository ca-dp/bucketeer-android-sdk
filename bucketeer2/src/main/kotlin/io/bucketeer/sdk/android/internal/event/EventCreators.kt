package io.bucketeer.sdk.android.internal.event

import io.bucketeer.sdk.android.internal.Clock
import io.bucketeer.sdk.android.internal.IdGenerator
import io.bucketeer.sdk.android.internal.model.Duration
import io.bucketeer.sdk.android.internal.model.Evaluation
import io.bucketeer.sdk.android.internal.model.Event
import io.bucketeer.sdk.android.internal.model.EventData
import io.bucketeer.sdk.android.internal.model.EventType
import io.bucketeer.sdk.android.internal.model.MetricsEventData
import io.bucketeer.sdk.android.internal.model.MetricsEventType
import io.bucketeer.sdk.android.internal.model.Reason
import io.bucketeer.sdk.android.internal.model.ReasonType
import io.bucketeer.sdk.android.internal.model.SourceID
import io.bucketeer.sdk.android.internal.model.User

internal fun newEvaluationEvent(
  clock: Clock,
  idGenerator: IdGenerator,
  featureTag: String,
  user: User,
  evaluation: Evaluation
): Event {
  return Event(
    id = idGenerator.newId(),
    type = EventType.EVALUATION,
    event = EventData.EvaluationEvent(
      timestamp = clock.currentTimeSeconds(),
      feature_id = evaluation.feature_id,
      feature_version = evaluation.feature_version,
      user_id = user.id,
      variation_id = evaluation.variation_id,
      user = user,
      reason = evaluation.reason,
      tag = featureTag,
      source_id = SourceID.ANDROID
    )
  )
}

internal fun newDefaultEvaluationEvent(
  clock: Clock,
  idGenerator: IdGenerator,
  featureTag: String,
  user: User,
  featureId: String
): Event {
  return Event(
    id = idGenerator.newId(),
    type = EventType.EVALUATION,
    event = EventData.EvaluationEvent(
      timestamp = clock.currentTimeSeconds(),
      feature_id = featureId,
      user_id = user.id,
      user = user,
      reason = Reason(
        type = ReasonType.CLIENT,
      ),
      tag = featureTag,
      source_id = SourceID.ANDROID,
    )
  )
}

internal fun newGoalEvent(
  clock: Clock,
  idGenerator: IdGenerator,
  goalId: String,
  value: Double,
  featureTag: String,
  user: User
): Event {
  return Event(
    id = idGenerator.newId(),
    type = EventType.GOAL,
    event = EventData.GoalEvent(
      timestamp = clock.currentTimeSeconds(),
      goal_id = goalId,
      user_id = user.id,
      value = value,
      user = user,
      tag = featureTag,
      source_id = SourceID.ANDROID
    ),
  )
}

internal fun newGetEvaluationLatencyMetricsEvent(
  clock: Clock,
  idGenerator: IdGenerator,
  mills: Long,
  featureTag: String,
): Event {
  return Event(
    id = idGenerator.newId(),
    type = EventType.METRICS,
    event = EventData.MetricsEvent(
      timestamp = clock.currentTimeSeconds(),
      type = MetricsEventType.GET_EVALUATION_LATENCY,
      event = MetricsEventData.GetEvaluationLatencyMetricsEvent(
        labels = mapOf(
          "tag" to featureTag,
        ),
        duration = Duration(millis = mills)
      )
    ),
  )
}

internal fun newGetEvaluationSizeMetricsEvent(
  clock: Clock,
  idGenerator: IdGenerator,
  sizeByte: Int,
  featureTag: String,
): Event {
  return Event(
    id = idGenerator.newId(),
    type = EventType.METRICS,
    event = EventData.MetricsEvent(
      timestamp = clock.currentTimeSeconds(),
      type = MetricsEventType.GET_EVALUATION_SIZE,
      event = MetricsEventData.GetEvaluationSizeMetricsEvent(
        labels = mapOf(
          "tag" to featureTag,
        ),
        size_byte = sizeByte
      )
    ),
  )
}

internal fun newTimeoutErrorCountMetricsEvent(
  clock: Clock,
  idGenerator: IdGenerator,
  featureTag: String
): Event {
  return Event(
    id = idGenerator.newId(),
    type = EventType.METRICS,
    event = EventData.MetricsEvent(
      timestamp = clock.currentTimeSeconds(),
      type = MetricsEventType.TIMEOUT_ERROR_COUNT,
      event = MetricsEventData.TimeoutErrorCountMetricsEvent(
        tag = featureTag
      )
    ),
  )
}

internal fun newInternalErrorCountMetricsEvent(
  clock: Clock,
  idGenerator: IdGenerator,
  featureTag: String
): Event {
  return Event(
    id = idGenerator.newId(),
    type = EventType.METRICS,
    event = EventData.MetricsEvent(
      timestamp = clock.currentTimeSeconds(),
      type = MetricsEventType.INTERNAL_ERROR_COUNT,
      event = MetricsEventData.InternalErrorCountMetricsEvent(
        tag = featureTag
      )
    ),
  )
}

// internal fun EventOuterClass.GoalEvent.toEvent(): EventOuterClass.Event {
//  return EventOuterClass.Event.newBuilder()
//    .setId(UUID.randomUUID().toString())
//    .setEvent(pack())
//    .build()
// }
//
// @VisibleForTesting
// internal fun EventOuterClass.GoalEvent.pack() =
//  pack("bucketeer.event.client.GoalEvent")
//
// internal fun EventOuterClass.EvaluationEvent.toEvent(): EventOuterClass.Event {
//  return EventOuterClass.Event.newBuilder()
//    .setId(UUID.randomUUID().toString())
//    .setEvent(pack())
//    .build()
// }
//
// @VisibleForTesting
// internal fun EventOuterClass.EvaluationEvent.pack() =
//  pack("bucketeer.event.client.EvaluationEvent")
//
// internal fun EventOuterClass.MetricsEvent.toEvent(): EventOuterClass.Event {
//  return EventOuterClass.Event.newBuilder()
//    .setId(UUID.randomUUID().toString())
//    .setEvent(pack())
//    .build()
// }
//
// @VisibleForTesting
// internal fun EventOuterClass.MetricsEvent.pack() =
//  pack("bucketeer.event.client.MetricsEvent")
//
// internal fun EventOuterClass.GetEvaluationLatencyMetricsEvent.toMetricsEvent(): EventOuterClass.MetricsEvent {
//  return EventOuterClass.MetricsEvent.newBuilder()
//    .setTimestamp(System.currentTimeMillis() / 1000)
//    .setEvent(pack())
//    .build()
// }
//
// @VisibleForTesting
// internal fun EventOuterClass.GetEvaluationLatencyMetricsEvent.pack() =
//  pack("bucketeer.event.client.GetEvaluationLatencyMetricsEvent")
//
// internal fun EventOuterClass.GetEvaluationSizeMetricsEvent.toMetricsEvent(): EventOuterClass.MetricsEvent {
//  return EventOuterClass.MetricsEvent.newBuilder()
//    .setTimestamp(System.currentTimeMillis() / 1000)
//    .setEvent(pack())
//    .build()
// }
//
// @VisibleForTesting
// internal fun EventOuterClass.GetEvaluationSizeMetricsEvent.pack() =
//  pack("bucketeer.event.client.GetEvaluationSizeMetricsEvent")
//
// internal fun EventOuterClass.InternalErrorCountMetricsEvent.toMetricsEvent(): EventOuterClass.MetricsEvent {
//  return EventOuterClass.MetricsEvent.newBuilder()
//    .setTimestamp(System.currentTimeMillis() / 1000)
//    .setEvent(pack())
//    .build()
// }
//
// @VisibleForTesting
// internal fun EventOuterClass.InternalErrorCountMetricsEvent.pack() =
//  pack("bucketeer.event.client.InternalErrorCountMetricsEvent")
//
// internal fun EventOuterClass.TimeoutErrorCountMetricsEvent.toMetricsEvent(): EventOuterClass.MetricsEvent {
//  return EventOuterClass.MetricsEvent.newBuilder()
//    .setTimestamp(System.currentTimeMillis() / 1000)
//    .setEvent(pack())
//    .build()
// }
//
// @VisibleForTesting
// internal fun EventOuterClass.TimeoutErrorCountMetricsEvent.pack() =
//  pack("bucketeer.event.client.TimeoutErrorCountMetricsEvent")
//
// private fun MessageLite.pack(
//  descriptor: String
// ): Any {
//  return Any.newBuilder()
//    .setTypeUrl(
//      getTypeUrl(
//        "type.googleapis.com",
//        descriptor
//      )
//    )
//    .setValue(toByteString())
//    .build()
// }
//
// private fun getTypeUrl(
//  typeUrlPrefix: String,
//  descriptor: String
// ): String {
//  return if (typeUrlPrefix.endsWith("/")) {
//    "$typeUrlPrefix$descriptor"
//  } else {
//    "$typeUrlPrefix/$descriptor"
//  }
// }
