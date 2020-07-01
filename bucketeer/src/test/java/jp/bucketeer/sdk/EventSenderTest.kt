package jp.bucketeer.sdk

import bucketeer.event.client.EventOuterClass
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import jp.bucketeer.sdk.events.EventActionCreator
import jp.bucketeer.sdk.events.EventStore
import jp.bucketeer.sdk.events.toEvent
import jp.bucketeer.sdk.util.ObservableField
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class EventSenderTest {

  private val eventObservableField = ObservableField<List<EventOuterClass.Event>>(mutableListOf())
  private val eventStore: EventStore = mock<EventStore>().also {
    whenever(it.events).doReturn(eventObservableField)
  }
  private val eventActionCreator = mock<EventActionCreator>().also {
    whenever(it.send(any(), any())).then {
      val nextEvents = eventObservableField.value.toMutableList()
      val it = nextEvents.iterator()
      var index = 0
      while (it.hasNext()) {
        it.next()
        if (index >= QUEUE_COUNT) {
          break
        }
        it.remove()
        index++
      }
      eventObservableField.value = nextEvents
      Unit
    }
  }

  @Test fun scheduleSending_sendByTimer() {
    val eventSender = createEventSenderWithInterval(50)
    val defaultValue = listOf(evaluationEvent1.toEvent(), goalEvent1.toEvent())

    eventSender.start()
    eventObservableField.value = defaultValue

    val callbackCountDown = CountDownLatch(1)
    val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    val scheduler = executor.scheduleWithFixedDelay(
        { callbackCountDown.countDown() },
        500,
        500,
        TimeUnit.MILLISECONDS
    )

    callbackCountDown.await(5, TimeUnit.SECONDS)
    scheduler.cancel(true)
    verify(eventActionCreator).send(defaultValue, QUEUE_COUNT)
    assertTrue(eventObservableField.value.isEmpty())
  }

  @Test fun scheduleSending_multipleSendByTimer() {
    val eventSender = createEventSenderWithInterval(50)
    val event100 = (0 until 100).map {
      listOf(evaluationEvent1.toEvent(), goalEvent1.toEvent())
    }.flatten()

    eventSender.start()
    eventObservableField.value = event100

    val callbackCountDown = CountDownLatch(1)
    val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    val scheduler = executor.scheduleWithFixedDelay(
        { callbackCountDown.countDown() },
        500,
        500,
        TimeUnit.MILLISECONDS
    )

    callbackCountDown.await(5, TimeUnit.SECONDS)
    scheduler.cancel(true)
    verify(eventActionCreator, times(4)).send(any(), any())
    assertTrue(eventObservableField.value.isEmpty())
  }

  @Test fun scheduleSending_notSendIfNotThatTimeOrCountOver() {
    val eventSender = createEventSenderWithInterval(5000)
    val defaultValue = listOf(evaluationEvent1.toEvent(), goalEvent1.toEvent())

    eventSender.start()
    eventObservableField.value = defaultValue

    val callbackCountDown = CountDownLatch(1)
    val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    val scheduler = executor.scheduleWithFixedDelay(
        { callbackCountDown.countDown() },
        500,
        500,
        TimeUnit.MILLISECONDS
    )

    callbackCountDown.await(5, TimeUnit.SECONDS)
    scheduler.cancel(true)
    verify(eventActionCreator, never()).send(defaultValue, QUEUE_COUNT)
    assertEquals(eventObservableField.value, defaultValue)
  }

  @Test fun scheduleSending_sendByEventCountOver() {
    val eventSender = createEventSenderWithInterval(5000)
    val event50 = (0 until 25).map {
      listOf(evaluationEvent1.toEvent(), goalEvent1.toEvent())
    }.flatten()

    eventSender.start()
    eventObservableField.value = event50

    val callbackCountDown = CountDownLatch(1)
    val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    val scheduler = executor.scheduleWithFixedDelay(
        { callbackCountDown.countDown() },
        500,
        500,
        TimeUnit.MILLISECONDS
    )

    callbackCountDown.await(5, TimeUnit.SECONDS)
    scheduler.cancel(true)
    verify(eventActionCreator).send(event50, 50)
    assertTrue(eventObservableField.value.isEmpty())
  }

  @Test
  fun scheduleSending_reschedule() {
    val eventSender = createEventSenderWithInterval(50)
    val event100 = (0 until 100).map {
      listOf(evaluationEvent1.toEvent(), goalEvent1.toEvent())
    }.flatten()

    eventSender.start()
    eventObservableField.value = event100

    val callbackCountDown = CountDownLatch(1)
    val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    val scheduler = executor.scheduleWithFixedDelay(
        { callbackCountDown.countDown() },
        500,
        500,
        TimeUnit.MILLISECONDS
    )

    callbackCountDown.await(5, TimeUnit.SECONDS)
    scheduler.cancel(true)
    verify(eventActionCreator, times(4)).send(any(), any())
    assertTrue(eventObservableField.value.isEmpty())
  }

  private fun createEventSenderWithInterval(
      logSendingIntervalMillis: Long): EventSender {
    eventObservableField.value = listOf()
    return EventSender(
        logSendingIntervalMillis,
        QUEUE_COUNT,
        eventActionCreator,
        eventStore,
        Executors.newSingleThreadScheduledExecutor()
    )
  }

  companion object {
    const val QUEUE_COUNT = 50
  }
}
