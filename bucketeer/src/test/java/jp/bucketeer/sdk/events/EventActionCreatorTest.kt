package jp.bucketeer.sdk.events

import bucketeer.event.client.EventOuterClass
import bucketeer.feature.ReasonOuterClass
import bucketeer.gateway.Service.RegisterEventsResponse
import com.google.protobuf.Any
import com.google.protobuf.Duration
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import jp.bucketeer.sdk.Api
import jp.bucketeer.sdk.ApiClient
import jp.bucketeer.sdk.dispatcher.Dispatcher
import jp.bucketeer.sdk.evaluation1
import jp.bucketeer.sdk.events.db.EventDao
import jp.bucketeer.sdk.events.dto.EventListDataChangedAction
import jp.bucketeer.sdk.metricsEvent1
import jp.bucketeer.sdk.toBucketeerException
import jp.bucketeer.sdk.user1
import org.junit.Assert
import org.amshove.kluent.any
import org.amshove.kluent.mock
import org.amshove.kluent.shouldEqual
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Observer

@RunWith(RobolectricTestRunner::class)
class EventActionCreatorTest {
  private val dispatcher: Dispatcher = Dispatcher()
  private val api: ApiClient = mock()
  private val eventDao: EventDao = mock()
  private val eventActionCreator = spy(EventActionCreator(api, dispatcher, eventDao))

  private val evaluationEvent1 = EventOuterClass.EvaluationEvent.newBuilder().apply {
    timestamp = 1234
    featureId = evaluation1.featureId
    featureVersion = evaluation1.featureVersion
    userId = user1.id
    variationId = evaluation1.variationId
    user = user1
    reason = ReasonOuterClass.Reason.newBuilder().setType(
        ReasonOuterClass.Reason.Type.DEFAULT).build()
  }.build()

  private val goalEvent1 = EventOuterClass.GoalEvent.newBuilder().apply {
    timestamp = 1234
    goalId = "goalId"
    userId = user1.id
    value = 100.0
    user = user1
    addAllEvaluations(listOf(evaluation1))
  }.build()

  private val metricsEvent1 = EventOuterClass.MetricsEvent
      .newBuilder()
      .setTimestamp(642128523) // 1990-05-08T01:02:03+00:00
      .setEvent(EventOuterClass.GetEvaluationLatencyMetricsEvent
          .newBuilder()
          .setDuration(Duration.newBuilder()
              .setSeconds(1)
              .setNanos(123456789))
          .putAllLabels(mapOf("tag" to "android", "state" to "FULL"))
          .build().pack())
      .build()

  private val metricsEvent2 = EventOuterClass.MetricsEvent
      .newBuilder()
      .setTimestamp(642128523) // 1990-05-08T01:02:03+00:00
      .setEvent(EventOuterClass.GetEvaluationSizeMetricsEvent
          .newBuilder()
          .setSizeByte(10000)
          .putAllLabels(mapOf("tag" to "android", "state" to "FULL"))
          .build().pack())
      .build()

  private val metricsEvent3 = EventOuterClass.MetricsEvent
      .newBuilder()
      .setTimestamp(642128523) // 1990-05-08T01:02:03+00:00
      .setEvent(EventOuterClass.InternalErrorCountMetricsEvent
          .newBuilder()
          .setTag("tag")
          .build().pack())
      .build()

  private val metricsEvent4 = EventOuterClass.MetricsEvent
      .newBuilder()
      .setTimestamp(642128523) // 1990-05-08T01:02:03+00:00
      .setEvent(EventOuterClass.TimeoutErrorCountMetricsEvent
          .newBuilder()
          .setTag("tag")
          .build().pack())
      .build()

  @Test fun pushEvaluationEvent_dispatchAddedEvent() {
    whenever(eventDao.getEvents()).thenReturn(
        listOf(EventOuterClass.Event.newBuilder()
            .setEvent(evaluationEvent1.pack())
            .build())
    )

    val mockObserver = spy(Observer { _, obj ->
      when (obj) {
        is EventListDataChangedAction -> obj.events.run {
          this.size shouldEqual 1
          val event = this.first().event.unpackToEvaluationEvent()
          event::class shouldEqual EventOuterClass.EvaluationEvent::class
        }
        else -> Assert.fail()
      }
    })
    dispatcher.addObserver(mockObserver)

    eventActionCreator.pushEvaluationEvent(1234, evaluation1, user1)
    verify(mockObserver).update(any(), any())
  }

  @Test fun pushEvaluationEvent_callEventDaoAddEvent() {
    val mockObserver: Observer = mock()
    dispatcher.addObserver(mockObserver)

    eventActionCreator.pushEvaluationEvent(1234, evaluation1, user1)
    verify(eventDao).addEvent(any<EventOuterClass.EvaluationEvent>())
    verify(mockObserver).update(any(), any())
  }

  @Test fun pushDefaultEvaluationEvent_callEventDaoAddEvent() {
    val mockObserver: Observer = mock()
    dispatcher.addObserver(mockObserver)

    eventActionCreator.pushDefaultEvaluationEvent(1234, user1, "feature id")

    verify(eventDao).addEvent(any<EventOuterClass.EvaluationEvent>())
    verify(mockObserver).update(any(), any())
  }

  @Test fun pushGoalEvent() {
    whenever(eventDao.getEvents()).thenReturn(
        listOf(EventOuterClass.Event.newBuilder()
            .setEvent(goalEvent1.pack())
            .build()))

    val mockObserver = spy(Observer { _, obj ->
      when (obj) {
        is EventListDataChangedAction -> {
          obj.events.run {
            this.size shouldEqual 1
            val event = this.first().event.unpackToGoalEvent()
            event::class shouldEqual EventOuterClass.GoalEvent::class
          }
        }
        else -> Assert.fail()
      }
    })
    dispatcher.addObserver(mockObserver)

    eventActionCreator.pushGoalEvent(1234, "goalId", 100.0, user1, listOf(evaluation1))
    verify(mockObserver).update(any(), any())
  }

  @Test fun pushGoalEvent_callEventDaoAddEvent() {
    val mockObserver: Observer = mock()
    dispatcher.addObserver(mockObserver)

    eventActionCreator.pushGoalEvent(1234, "goalId", 100.0, user1, listOf(evaluation1))
    verify(eventDao).addEvent(any<EventOuterClass.GoalEvent>())
    verify(mockObserver).update(any(), any())
  }

  @Test fun pushGetEvaluationLatencyMetricsEvent() {
    whenever(eventDao.getEvents()).thenReturn(
        listOf(EventOuterClass.Event.newBuilder()
            .setEvent(metricsEvent1.pack())
            .build()))

    val mockObserver = spy(Observer { _, obj ->
      when (obj) {
        is EventListDataChangedAction -> {
          obj.events.run {
            this.size shouldEqual 1
            val event = this.first().event.unpackToMetricsEvent()
            event::class shouldEqual EventOuterClass.MetricsEvent::class
          }
        }
        else -> Assert.fail()
      }
    })
    dispatcher.addObserver(mockObserver)

    eventActionCreator.pushGetEvaluationLatencyMetricsEvent(1234,
        mapOf("tag" to "android", "state" to "FULL"))
    verify(mockObserver).update(any(), any())
  }

  @Test fun pushGetEvaluationLatencyMetricsEvent_callEventDaoAddEvent() {
    val mockObserver: Observer = mock()
    dispatcher.addObserver(mockObserver)

    eventActionCreator.pushGetEvaluationLatencyMetricsEvent(1234,
        mapOf("tag" to "android", "state" to "FULL"))
    verify(eventDao).addEvent(any<EventOuterClass.MetricsEvent>())
    verify(mockObserver).update(any(), any())
  }

  @Test fun pushGetEvaluationSizeMetricsEvent() {
    whenever(eventDao.getEvents()).thenReturn(
        listOf(EventOuterClass.Event.newBuilder()
            .setEvent(metricsEvent2.pack())
            .build()))

    val mockObserver = spy(Observer { _, obj ->
      when (obj) {
        is EventListDataChangedAction -> {
          obj.events.run {
            this.size shouldEqual 1
            val event = this.first().event.unpackToMetricsEvent()
            event::class shouldEqual EventOuterClass.MetricsEvent::class
          }
        }
        else -> Assert.fail()
      }
    })
    dispatcher.addObserver(mockObserver)

    eventActionCreator.pushGetEvaluationSizeMetricsEvent(10000,
        mapOf("tag" to "android", "state" to "FULL"))
    verify(mockObserver).update(any(), any())
  }

  @Test fun pushGetEvaluationSizeMetricsEvent_callEventDaoAddEvent() {
    val mockObserver: Observer = mock()
    dispatcher.addObserver(mockObserver)

    eventActionCreator.pushGetEvaluationSizeMetricsEvent(10000,
        mapOf("tag" to "android", "state" to "FULL"))
    verify(eventDao).addEvent(any<EventOuterClass.MetricsEvent>())
    verify(mockObserver).update(any(), any())
  }

  @Test fun pushTimeoutErrorCountMetricsEvent() {
    whenever(eventDao.getEvents()).thenReturn(
        listOf(EventOuterClass.Event.newBuilder()
            .setEvent(metricsEvent3.pack())
            .build()))

    val mockObserver = spy(Observer { _, obj ->
      when (obj) {
        is EventListDataChangedAction -> {
          obj.events.run {
            this.size shouldEqual 1
            val event = this.first().event.unpackToMetricsEvent()
            event::class shouldEqual EventOuterClass.MetricsEvent::class
          }
        }
        else -> Assert.fail()
      }
    })
    dispatcher.addObserver(mockObserver)

    eventActionCreator.pushTimeoutErrorCountMetricsEvent("tag")
    verify(mockObserver).update(any(), any())
  }

  @Test fun pushTimeoutErrorCountMetricsEvent_callEventDaoAddEvent() {
    val mockObserver: Observer = mock()
    dispatcher.addObserver(mockObserver)

    eventActionCreator.pushTimeoutErrorCountMetricsEvent("tag")
    verify(eventDao).addEvent(any<EventOuterClass.MetricsEvent>())
    verify(mockObserver).update(any(), any())
  }

  @Test fun pushInternalErrorCountMetricsEvent() {
    whenever(eventDao.getEvents()).thenReturn(
        listOf(EventOuterClass.Event.newBuilder()
            .setEvent(metricsEvent4.pack())
            .build()))

    val mockObserver = spy(Observer { _, obj ->
      when (obj) {
        is EventListDataChangedAction -> {
          obj.events.run {
            this.size shouldEqual 1
            val event = this.first().event.unpackToMetricsEvent()
            event::class shouldEqual EventOuterClass.MetricsEvent::class
          }
        }
        else -> Assert.fail()
      }
    })
    dispatcher.addObserver(mockObserver)

    eventActionCreator.pushInternalErrorCountMetricsEvent("tag")
    verify(mockObserver).update(any(), any())
  }

  @Test fun pushInternalErrorCountMetricsEvent_callEventDaoAddEvent() {
    val mockObserver: Observer = mock()
    dispatcher.addObserver(mockObserver)

    eventActionCreator.pushInternalErrorCountMetricsEvent("tag")
    verify(eventDao).addEvent(any<EventOuterClass.MetricsEvent>())
    verify(mockObserver).update(any(), any())
  }

  @Test fun eventListDataChangedActionCanContainAllEvents() {
    eventActionCreator.pushEvaluationEvent(1234, evaluation1, user1)

    whenever(eventDao.getEvents()).thenReturn(
        listOf(
            evaluationEvent1.toEvent(),
            goalEvent1.toEvent(),
            metricsEvent1.toEvent(),
            metricsEvent2.toEvent(),
            metricsEvent3.toEvent(),
            metricsEvent4.toEvent()
        )
    )

    val spyObserver: Observer = spy(Observer { _, obj ->
      when (obj) {
        is EventListDataChangedAction -> {
          obj.events.size shouldEqual 6
          obj.events[0].event.unpackToEvaluationEvent()
          obj.events[1].event.unpackToGoalEvent()
          obj.events[2].event.unpackToMetricsEvent()
          obj.events[3].event.unpackToMetricsEvent()
          obj.events[4].event.unpackToMetricsEvent()
          obj.events[5].event.unpackToMetricsEvent()
        }
      }
    })
    dispatcher.addObserver(spyObserver)

    eventActionCreator.pushGoalEvent(
        timestamp = goalEvent1.timestamp,
        goalId = goalEvent1.goalId,
        value = goalEvent1.value,
        user = user1,
        evaluations = goalEvent1.evaluationsList)

    verify(spyObserver).update(any(), any())
  }

  @Test fun send_notRegisterEvent_when_event_empty() {
    eventActionCreator.send(emptyList(), 50)

    verifyNoMoreInteractions(api, eventDao)
  }

  @Test fun send_deleteAllEvent_afterRegisterEvent() {
    val value = RegisterEventsResponse.newBuilder().build()
    val events = listOf(evaluationEvent1.toEvent(), goalEvent1.toEvent())
    whenever(api.registerEvent(events)).thenReturn(Api.Result.Success(value))

    eventActionCreator.send(events, 50)

    verify(eventDao).delete(listOf(events[0].id, events[1].id))
  }

  @Test fun send_notDeleteRetriableEvent_afterRegisterEvent() {
    val events = listOf(evaluationEvent1.toEvent(), goalEvent1.toEvent())
    val value = RegisterEventsResponse.newBuilder()
        .putErrors(events[0].id,
            RegisterEventsResponse.Error.newBuilder().setRetriable(true).build())
        .build()

    whenever(api.registerEvent(events)).thenReturn(Api.Result.Success(value))

    eventActionCreator.send(events, 50)

    verify(eventDao).delete(listOf(events[1].id))
  }

  @Test fun send_deleteNotRetriableEvent_afterRegisterEvent() {
    val events = listOf(evaluationEvent1.toEvent(), goalEvent1.toEvent())
    val value = RegisterEventsResponse.newBuilder()
        .putErrors(events[0].id,
            RegisterEventsResponse.Error.newBuilder().setRetriable(false).build())
        .build()
    whenever(api.registerEvent(events)).thenReturn(Api.Result.Success(value))

    eventActionCreator.send(events, 50)

    verify(eventDao).delete(listOf(events[0].id, events[1].id))
  }

  @Test fun send_notDeleteEvents_when_apiFailed() {
    val events = listOf(evaluationEvent1.toEvent(), goalEvent1.toEvent())
    whenever(api.registerEvent(events)).thenReturn(
        Api.Result.Fail(Exception("exception").toBucketeerException()))

    eventActionCreator.send(events, 50)

    verify(api).registerEvent(events)
    verifyNoMoreInteractions(api, eventDao)
  }

  private fun Any.unpackToEvaluationEvent(): EventOuterClass.EvaluationEvent {
    return EventOuterClass.EvaluationEvent.parseFrom(this.value)
  }

  private fun Any.unpackToGoalEvent(): EventOuterClass.GoalEvent {
    return EventOuterClass.GoalEvent.parseFrom(this.value)
  }

  private fun Any.unpackToMetricsEvent(): EventOuterClass.MetricsEvent {
    return EventOuterClass.MetricsEvent.parseFrom(this.value)
  }
}
