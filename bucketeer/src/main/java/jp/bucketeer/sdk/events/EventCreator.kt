package jp.bucketeer.sdk.events

import bucketeer.event.client.EventOuterClass
import bucketeer.feature.EvaluationOuterClass
import bucketeer.feature.ReasonOuterClass
import bucketeer.user.UserOuterClass
import com.google.protobuf.Duration

val NANOS_PER_MILLISECOND: Long = 1000000
val MILLIS_PER_SECOND: Long = 1000

internal fun generateGoalEvent(
    timestamp: Long,
    goalId: String,
    value: Double,
    user: UserOuterClass.User,
    evaluations: List<EvaluationOuterClass.Evaluation>
): EventOuterClass.GoalEvent {
  return EventOuterClass.GoalEvent.newBuilder().apply {
    this.timestamp = timestamp
    this.goalId = goalId
    this.userId = user.id
    this.value = value
    this.user = user
    addAllEvaluations(evaluations)
  }.build()
}

internal fun generateEvaluationEvent(
    timestamp: Long,
    evaluation: EvaluationOuterClass.Evaluation,
    user: UserOuterClass.User
): EventOuterClass.EvaluationEvent {
  return EventOuterClass.EvaluationEvent.newBuilder().apply {
    this.timestamp = timestamp
    this.featureId = evaluation.featureId
    this.featureVersion = evaluation.featureVersion
    this.userId = user.id
    this.variationId = evaluation.variationId
    this.user = user
    this.reason = evaluation.reason
  }.build()
}

internal fun generateDefaultEvaluationEvent(
    timestamp: Long,
    user: UserOuterClass.User,
    featureId: String
): EventOuterClass.EvaluationEvent {
  val reason = ReasonOuterClass.Reason.newBuilder()
      .setType(ReasonOuterClass.Reason.Type.CLIENT)
      .build()
  return EventOuterClass.EvaluationEvent.newBuilder().apply {
    this.timestamp = timestamp
    this.featureId = featureId
    this.userId = user.id
    this.user = user
    this.reason = reason
  }.build()
}

internal fun generateGetEvaluationLatencyMetricsEvent(
    latencyMillis: Long,
    labels: Map<String, String>
): EventOuterClass.MetricsEvent {
  return EventOuterClass.GetEvaluationLatencyMetricsEvent.newBuilder().apply {
    this.duration = Duration.newBuilder()
        .setSeconds(latencyMillis / MILLIS_PER_SECOND)
        .setNanos((latencyMillis % MILLIS_PER_SECOND * NANOS_PER_MILLISECOND).toInt())
        .build()
  }.putAllLabels(labels)
      .build().toMetricsEvent()
}

internal fun generateGetEvaluationSizeMetricsEvent(
    sizeByte: Int,
    labels: Map<String, String>
): EventOuterClass.MetricsEvent {
  return EventOuterClass.GetEvaluationSizeMetricsEvent.newBuilder().apply {
    this.sizeByte = sizeByte
  }.putAllLabels(labels)
      .build().toMetricsEvent()
}

internal fun generateInternalErrorCountMetricsEvent(tag: String): EventOuterClass.MetricsEvent {
  return EventOuterClass.InternalErrorCountMetricsEvent.newBuilder().apply {
    this.tag = tag
  }.build().toMetricsEvent()
}

internal fun generateTimeoutErrorCountMetricsEvent(tag: String): EventOuterClass.MetricsEvent {
  return EventOuterClass.TimeoutErrorCountMetricsEvent.newBuilder().apply {
    this.tag = tag
  }.build().toMetricsEvent()
}
