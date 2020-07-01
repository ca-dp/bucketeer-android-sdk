package jp.bucketeer.sdk.events.db

import bucketeer.event.client.EventOuterClass

internal interface EventDao {
  fun addEvent(goalEvent: EventOuterClass.GoalEvent)
  fun addEvent(evaluationEvent: EventOuterClass.EvaluationEvent)
  fun addEvent(metricsEvent: EventOuterClass.MetricsEvent)
  fun getEvents(): List<EventOuterClass.Event>

  /** delete rows by ID */
  fun delete(ids: List<String>)
}
