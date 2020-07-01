package jp.bucketeer.sdk.dispatcher

import java.util.Observable
import kotlin.properties.Delegates

internal class Dispatcher : Observable() {
  var value: Any? by Delegates.observable<Any?>(null) { _, _, new ->
    setChanged()
    notifyObservers(new)
  }

  fun send(any: Any) {
    value = any
  }
}
