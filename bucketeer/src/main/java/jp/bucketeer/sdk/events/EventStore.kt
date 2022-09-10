package jp.bucketeer.sdk.events

import bucketeer.event.client.EventOuterClass
import jp.bucketeer.sdk.dispatcher.Dispatcher
import jp.bucketeer.sdk.events.dto.EventListDataChangedAction
import jp.bucketeer.sdk.util.ObservableField
import java.util.Observable

internal class EventStore(val dispatcher: Dispatcher) {
  val events: ObservableField<List<EventOuterClass.Event>> = ObservableField(listOf())

  init {
    dispatcher.addObserver({ _: Observable?, arg: Any? ->
      when (arg) {
        is EventListDataChangedAction ->
          // Because it loops endlessly
          // when event is exactly 50 and retriable = true is returned from the server
          if (events.value != arg.events) {
            events.value = arg.events
          }
      }
    },)
  }
}
