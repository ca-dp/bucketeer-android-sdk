package jp.bucketeer.sdk.util

import kotlin.properties.Delegates

internal class ObservableField<T>(defaultValue: T) {
  private val valueObservers = mutableListOf<(T) -> Unit>()

  fun addObserver(observer: (T) -> Unit) = synchronized(this) {
    if (!valueObservers.contains(observer)) {
      valueObservers += observer
    }
  }

  fun addOneTimeObserver(observer: (T) -> Unit) = synchronized(this) {
    valueObservers += object : (T) -> Unit {
      override fun invoke(p1: T) {
        observer.invoke(p1)
        removeObserver(this)
      }
    }
  }

  fun removeObserver(observer: (T) -> Unit) = synchronized(this) {
    valueObservers -= observer
  }

  var value: T by Delegates.observable(defaultValue) { _, _, new ->
    val list = synchronized(this) {
      valueObservers.toList()
    }
    list.forEach { it(new) }
  }
}
