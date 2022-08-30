package io.bucketeer.sdk.android.internal.event.db

import io.bucketeer.sdk.android.internal.model.Event

internal interface EventDao {
  //  fun addEvent(goalEvent: Event<EventData.GoalEvent>)
//  fun addEvent(evaluationEvent: Event<EventData.EvaluationEvent>)
//
//  fun addEvent(metricsEvent: Event<EventData.MetricsEvent>)
  fun addEvent(event: Event)
  fun addEvents(events: List<Event>)
  fun getEvents(): List<Event>

  /** delete rows by ID */
  fun delete(ids: List<String>)
}
