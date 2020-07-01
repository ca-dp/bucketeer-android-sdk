package jp.bucketeer.sdk.events

import bucketeer.event.client.EventOuterClass
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import jp.bucketeer.sdk.dispatcher.Dispatcher
import jp.bucketeer.sdk.evaluationEvent1
import jp.bucketeer.sdk.events.dto.EventListDataChangedAction
import org.junit.Test

class EventStoreTest {
  @Test
  fun addObserver_NotNotifyIfSameEvent() {
    val dispatcher = Dispatcher()
    val eventStore = EventStore(dispatcher)
    val observer = mock<(List<EventOuterClass.Event>) -> Unit>()
    eventStore.events.addObserver(observer)
    val events = listOf(evaluationEvent1.toEvent())

    dispatcher.send(EventListDataChangedAction(events))
    dispatcher.send(EventListDataChangedAction(events))

    verify(observer, times(1)).invoke(events)
  }
}
