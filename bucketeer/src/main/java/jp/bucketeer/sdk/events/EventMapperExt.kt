package jp.bucketeer.sdk.events

import androidx.annotation.VisibleForTesting
import bucketeer.event.client.EventOuterClass
import com.google.protobuf.Any
import com.google.protobuf.MessageLite
import java.util.UUID

internal fun EventOuterClass.GoalEvent.toEvent(): EventOuterClass.Event {
  return EventOuterClass.Event.newBuilder()
    .setId(UUID.randomUUID().toString())
    .setEvent(pack())
    .build()
}

@VisibleForTesting
internal fun EventOuterClass.GoalEvent.pack() =
  pack("bucketeer.event.client.GoalEvent")

internal fun EventOuterClass.EvaluationEvent.toEvent(): EventOuterClass.Event {
  return EventOuterClass.Event.newBuilder()
    .setId(UUID.randomUUID().toString())
    .setEvent(pack())
    .build()
}

@VisibleForTesting
internal fun EventOuterClass.EvaluationEvent.pack() =
  pack("bucketeer.event.client.EvaluationEvent")

internal fun EventOuterClass.MetricsEvent.toEvent(): EventOuterClass.Event {
  return EventOuterClass.Event.newBuilder()
    .setId(UUID.randomUUID().toString())
    .setEvent(pack())
    .build()
}

@VisibleForTesting
internal fun EventOuterClass.MetricsEvent.pack() =
  pack("bucketeer.event.client.MetricsEvent")

internal fun EventOuterClass.GetEvaluationLatencyMetricsEvent.toMetricsEvent(): EventOuterClass.MetricsEvent {
  return EventOuterClass.MetricsEvent.newBuilder()
    .setTimestamp(System.currentTimeMillis() / 1000)
    .setEvent(pack())
    .build()
}

@VisibleForTesting
internal fun EventOuterClass.GetEvaluationLatencyMetricsEvent.pack() =
  pack("bucketeer.event.client.GetEvaluationLatencyMetricsEvent")

internal fun EventOuterClass.GetEvaluationSizeMetricsEvent.toMetricsEvent(): EventOuterClass.MetricsEvent {
  return EventOuterClass.MetricsEvent.newBuilder()
    .setTimestamp(System.currentTimeMillis() / 1000)
    .setEvent(pack())
    .build()
}

@VisibleForTesting
internal fun EventOuterClass.GetEvaluationSizeMetricsEvent.pack() =
  pack("bucketeer.event.client.GetEvaluationSizeMetricsEvent")

internal fun EventOuterClass.InternalErrorCountMetricsEvent.toMetricsEvent(): EventOuterClass.MetricsEvent {
  return EventOuterClass.MetricsEvent.newBuilder()
    .setTimestamp(System.currentTimeMillis() / 1000)
    .setEvent(pack())
    .build()
}

@VisibleForTesting
internal fun EventOuterClass.InternalErrorCountMetricsEvent.pack() =
  pack("bucketeer.event.client.InternalErrorCountMetricsEvent")

internal fun EventOuterClass.TimeoutErrorCountMetricsEvent.toMetricsEvent(): EventOuterClass.MetricsEvent {
  return EventOuterClass.MetricsEvent.newBuilder()
    .setTimestamp(System.currentTimeMillis() / 1000)
    .setEvent(pack())
    .build()
}

@VisibleForTesting
internal fun EventOuterClass.TimeoutErrorCountMetricsEvent.pack() =
  pack("bucketeer.event.client.TimeoutErrorCountMetricsEvent")

private fun MessageLite.pack(
  descriptor: String
): Any {
  return Any.newBuilder()
    .setTypeUrl(
      getTypeUrl(
        "type.googleapis.com",
        descriptor
      )
    )
    .setValue(toByteString())
    .build()
}

private fun getTypeUrl(
  typeUrlPrefix: String,
  descriptor: String
): String {
  return if (typeUrlPrefix.endsWith("/")) {
    "$typeUrlPrefix$descriptor"
  } else {
    "$typeUrlPrefix/$descriptor"
  }
}
