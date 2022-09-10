package jp.bucketeer.sdk.events

import bucketeer.event.client.EventOuterClass
import bucketeer.feature.EvaluationOuterClass
import bucketeer.user.UserOuterClass
import jp.bucketeer.sdk.Api
import jp.bucketeer.sdk.dispatcher.Dispatcher
import jp.bucketeer.sdk.events.db.EventDao
import jp.bucketeer.sdk.events.dto.EventListDataChangedAction
import jp.bucketeer.sdk.log.logd

internal class EventActionCreator(
  private val api: Api,
  private val dispatcher: Dispatcher,
  private val eventDao: EventDao,
  private val featureTag: String,
) {
  fun pushEvaluationEvent(
    timestamp: Long,
    evaluation: EvaluationOuterClass.Evaluation,
    user: UserOuterClass.User,
  ) {
    val event = generateEvaluationEvent(featureTag, timestamp, evaluation, user)
    eventDao.addEvent(event)
    val events = getAllEvent()
    dispatcher.send(EventListDataChangedAction(events))
  }

  fun pushDefaultEvaluationEvent(
    timestamp: Long,
    user: UserOuterClass.User,
    featureId: String,
  ) {
    val event = generateDefaultEvaluationEvent(featureTag, timestamp, user, featureId)
    eventDao.addEvent(event)
    val events = getAllEvent()
    dispatcher.send(EventListDataChangedAction(events))
  }

  private fun getAllEvent(): List<EventOuterClass.Event> = eventDao.getEvents()

  fun pushGoalEvent(
    timestamp: Long,
    goalId: String,
    value: Double,
    user: UserOuterClass.User,
    evaluations: List<EvaluationOuterClass.Evaluation>,
  ) {
    val event = generateGoalEvent(featureTag, timestamp, goalId, value, user, evaluations)
    eventDao.addEvent(event)
    val events = getAllEvent()
    dispatcher.send(EventListDataChangedAction(events))
  }

  fun pushGetEvaluationLatencyMetricsEvent(
    latencyMills: Long,
    labels: Map<String, String>,
  ) {
    val event = generateGetEvaluationLatencyMetricsEvent(latencyMills, labels)
    eventDao.addEvent(event)
    val events = getAllEvent()
    dispatcher.send(EventListDataChangedAction(events))
  }

  fun pushGetEvaluationSizeMetricsEvent(
    sizeByte: Int,
    labels: Map<String, String>,
  ) {
    val event = generateGetEvaluationSizeMetricsEvent(sizeByte, labels)
    eventDao.addEvent(event)
    val events = getAllEvent()
    dispatcher.send(EventListDataChangedAction(events))
  }

  fun pushInternalErrorCountMetricsEvent(tag: String) {
    val event = generateInternalErrorCountMetricsEvent(tag)
    eventDao.addEvent(event)
    val events = getAllEvent()
    dispatcher.send(EventListDataChangedAction(events))
  }

  fun pushTimeoutErrorCountMetricsEvent(tag: String) {
    val event = generateTimeoutErrorCountMetricsEvent(tag)
    eventDao.addEvent(event)
    val events = getAllEvent()
    dispatcher.send(EventListDataChangedAction(events))
  }

  fun send(
    events: List<EventOuterClass.Event>,
    logSendingMaxBatchQueueCount: Int,
  ) {
    if (events.isEmpty()) return
    val sendingEvents = events.take(logSendingMaxBatchQueueCount).toList()
    val registerEvent = api.registerEvent(sendingEvents)
    when (registerEvent) {
      is Api.Result.Success -> {
        val errorMap = registerEvent.value.errorsMap
        val deleteIds = sendingEvents.map(EventOuterClass.Event::getId)
          .filter {
            // if the event does not contain in error, delete it
            val error = errorMap[it] ?: return@filter true
            // if the error is not retriable, delete it
            !error.retriable
          }

        eventDao.delete(deleteIds)
        dispatcher.send(EventListDataChangedAction(getAllEvent()))
      }
      is Api.Result.Fail -> {
        logd(throwable = registerEvent.e) { "EventActionCreator: registerEvent Error" }
      }
    }
  }
}
