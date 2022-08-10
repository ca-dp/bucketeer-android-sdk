package jp.bucketeer.sdk

import bucketeer.event.client.EventOuterClass
import bucketeer.feature.EvaluationOuterClass
import bucketeer.feature.ReasonOuterClass
import bucketeer.gateway.Service
import bucketeer.user.UserOuterClass
import com.google.protobuf.Duration
import jp.bucketeer.sdk.events.pack

val userEvaluationsId1: String by lazy {
  "user-evaluations-id-1"
}

val userEvaluationsId2: String by lazy {
  "user-evaluations-id-2"
}

val user: User by lazy {
  User(
    "user-id",
    mapOf(
      "gender" to "male",
      "age" to "40"
    )
  )
}

val user1: UserOuterClass.User by lazy {
  UserOuterClass.User.newBuilder()
    .setId("user id 1")
    .putAllData(mapOf("age" to "28"))
    .build()
}

val user2: UserOuterClass.User by lazy {
  UserOuterClass.User.newBuilder()
    .setId("user id 2")
    .build()
}

val responsePartial: Service.GetEvaluationsResponse by lazy {
  Service.GetEvaluationsResponse.newBuilder().apply {
    state = EvaluationOuterClass.UserEvaluations.State.PARTIAL
    evaluations = user1Evaluations
    userEvaluationsId = userEvaluationsId1
  }.build()
}

val responseFull: Service.GetEvaluationsResponse by lazy {
  Service.GetEvaluationsResponse.newBuilder().apply {
    state = EvaluationOuterClass.UserEvaluations.State.FULL
    evaluations = user1Evaluations
    userEvaluationsId = userEvaluationsId1
  }.build()
}

val evaluation: Evaluation by lazy {
  Evaluation(
    evaluation1.id,
    evaluation1.featureId,
    evaluation1.featureVersion,
    evaluation1.userId,
    evaluation1.variationId,
    evaluation1.variationValue,
    evaluation1.reason.type.number
  )
}

val evaluation1: EvaluationOuterClass.Evaluation by lazy {
  EvaluationOuterClass.Evaluation.newBuilder().apply {
    id = "test-feature-1:9:user id 1"
    featureId = "test-feature-1"
    featureVersion = 9
    userId = "user id 1"
    variationId = "test-feature-1-variation-A"
    variationValue = "test variation value1"

    reason = ReasonOuterClass.Reason.newBuilder().apply {
      type = ReasonOuterClass.Reason.Type.DEFAULT
    }.build()
  }.build()
}

val evaluation2: EvaluationOuterClass.Evaluation by lazy {
  EvaluationOuterClass.Evaluation.newBuilder().apply {
    id = "test-feature-2:9:user id 1"
    featureId = "test-feature-2"
    featureVersion = 9
    userId = "user id 1"
    variationId = "test-feature-2-variation-A"
    variationValue = "test variation value2"

    reason = ReasonOuterClass.Reason.newBuilder().apply {
      type = ReasonOuterClass.Reason.Type.DEFAULT
    }.build()
  }.build()
}

val evaluation3: EvaluationOuterClass.Evaluation by lazy {
  EvaluationOuterClass.Evaluation.newBuilder().apply {
    id = "test-feature-1:9:user id 2"
    featureId = "test-feature-3"
    featureVersion = 9
    userId = "user id 2"
    variationId = "test-feature-1-variation-A"
    variationValue = "test variation value2"

    reason = ReasonOuterClass.Reason.newBuilder().apply {
      type = ReasonOuterClass.Reason.Type.DEFAULT
    }.build()
  }.build()
}

val user1Evaluations: EvaluationOuterClass.UserEvaluations by lazy {
  EvaluationOuterClass.UserEvaluations.newBuilder().apply {
    id = "17388826713971171773"
    addEvaluations(evaluation1)
    addEvaluations(evaluation2)
  }.build()
}

val user2Evaluations: EvaluationOuterClass.UserEvaluations by lazy {
  EvaluationOuterClass.UserEvaluations.newBuilder().apply {
    id = "17388826713971171774"
    addEvaluations(evaluation3)
  }.build()
}

val evaluationEvent1 = EventOuterClass.EvaluationEvent
  .newBuilder()
  .setFeatureId("evaluation1")
  .build()

val evaluationEvent2 = EventOuterClass.EvaluationEvent
  .newBuilder()
  .setFeatureId("evaluation2")
  .build()

val goalEvent1 = EventOuterClass.GoalEvent
  .newBuilder()
  .setGoalId("goal1")
  .addAllEvaluations(user1Evaluations.evaluationsList)
  .build()

val goalEvent2 = EventOuterClass.GoalEvent
  .newBuilder()
  .setGoalId("goal2")
  .addAllEvaluations(user1Evaluations.evaluationsList)
  .build()

val getEvaluationLatencyMetricsEvent1 = EventOuterClass.GetEvaluationLatencyMetricsEvent
  .newBuilder()
  .setDuration(
    Duration.newBuilder()
      .setSeconds(1)
      .setNanos(123456789)
  )
  .putAllLabels(mapOf("tag" to "android", "state" to "FULL"))
  .build()

val getEvaluationLatencyMetricsEvent2 = EventOuterClass.GetEvaluationLatencyMetricsEvent
  .newBuilder()
  .setDuration(
    Duration.newBuilder()
      .setSeconds(2)
      .setNanos(987654321)
  )
  .putAllLabels(mapOf("tag" to "firetv", "state" to "PARTIAL"))
  .build()

val getEvaluationSizeMetricsEvent1 = EventOuterClass.GetEvaluationSizeMetricsEvent
  .newBuilder()
  .setSizeByte(10000)
  .putAllLabels(mapOf("tag" to "android", "state" to "FULL"))
  .build()

val getEvaluationSizeMetricsEvent2 = EventOuterClass.GetEvaluationSizeMetricsEvent
  .newBuilder()
  .setSizeByte(20000)
  .putAllLabels(mapOf("tag" to "android", "state" to "FULL"))
  .build()

val internalErrorCountMetricsEvent1 = EventOuterClass.InternalErrorCountMetricsEvent
  .newBuilder()
  .setTag("tag1")
  .build()

val internalErrorCountMetricsEvent2 = EventOuterClass.InternalErrorCountMetricsEvent
  .newBuilder()
  .setTag("tag2")
  .build()

val timeoutErrorCountMetricsEvent1 = EventOuterClass.TimeoutErrorCountMetricsEvent
  .newBuilder()
  .setTag("tag1")
  .build()

val timeoutErrorCountMetricsEvent2 = EventOuterClass.TimeoutErrorCountMetricsEvent
  .newBuilder()
  .setTag("tag2")
  .build()

val metricsEvent1 = EventOuterClass.MetricsEvent
  .newBuilder()
  .setTimestamp(642128523) // 1990-05-08T01:02:03+00:00
  .setEvent(getEvaluationLatencyMetricsEvent1.pack())
  .build()

val metricsEvent2 = EventOuterClass.MetricsEvent
  .newBuilder()
  .setTimestamp(724554123) // 1992-12-17T01:02:03+00:00
  .setEvent(getEvaluationLatencyMetricsEvent2.pack())
  .build()

val metricsEvent3 = EventOuterClass.MetricsEvent
  .newBuilder()
  .setTimestamp(642128523) // 1990-05-08T01:02:03+00:00
  .setEvent(getEvaluationSizeMetricsEvent1.pack())
  .build()

val metricsEvent4 = EventOuterClass.MetricsEvent
  .newBuilder()
  .setTimestamp(724554123) // 1992-12-17T01:02:03+00:00
  .setEvent(getEvaluationSizeMetricsEvent2.pack())
  .build()

val metricsEvent5 = EventOuterClass.MetricsEvent
  .newBuilder()
  .setTimestamp(642128523) // 1990-05-08T01:02:03+00:00
  .setEvent(internalErrorCountMetricsEvent1.pack())
  .build()

val metricsEvent6 = EventOuterClass.MetricsEvent
  .newBuilder()
  .setTimestamp(724554123) // 1992-12-17T01:02:03+00:00
  .setEvent(internalErrorCountMetricsEvent2.pack())
  .build()

val metricsEvent7 = EventOuterClass.MetricsEvent
  .newBuilder()
  .setTimestamp(642128523) // 1990-05-08T01:02:03+00:00
  .setEvent(timeoutErrorCountMetricsEvent1.pack())
  .build()

val metricsEvent8 = EventOuterClass.MetricsEvent
  .newBuilder()
  .setTimestamp(724554123) // 1992-12-17T01:02:03+00:00
  .setEvent(timeoutErrorCountMetricsEvent2.pack())
  .build()
