package jp.bucketeer.sdk

import bucketeer.event.client.EventOuterClass
import jp.bucketeer.sdk.events.EventActionCreator
import jp.bucketeer.sdk.events.EventStore
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

internal class EventSender(
  private val logSendingIntervalMillis: Long,
  private val logSendingMaxBatchQueueCount: Int,
  private val eventActionCreator: EventActionCreator,
  private val eventStore: EventStore,
  private val scheduledExecutorService: ScheduledExecutorService
) : ScheduledTask {
  override var isStarted: Boolean = false
  private var scheduledFuture: ScheduledFuture<*>? = null

  private val eventObserver = { events: List<EventOuterClass.Event> ->
    if (events.size == logSendingMaxBatchQueueCount) {
      sendEvent()
      reschedule()
    }
  }

  override fun start() {
    isStarted = true
    eventStore.events.addObserver(eventObserver)
    reschedule()
  }

  private fun reschedule() {
    scheduledFuture?.cancel(false)
    scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(
      { sendEvent() },
      logSendingIntervalMillis,
      logSendingIntervalMillis,
      TimeUnit.MILLISECONDS
    )
  }

  override fun stop() {
    isStarted = false
    eventStore.events.removeObserver(eventObserver)
    scheduledFuture?.cancel(false)
  }

  private fun sendEvent() {
    if (eventStore.events.value.isEmpty()) return
    eventActionCreator.send(eventStore.events.value, logSendingMaxBatchQueueCount)
  }
}
