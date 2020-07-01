package jp.bucketeer.sdk.di

import jp.bucketeer.sdk.evaluation.CurrentStore
import jp.bucketeer.sdk.dispatcher.Dispatcher
import jp.bucketeer.sdk.events.EventStore
import jp.bucketeer.sdk.evaluation.LatestEvaluationStore

internal class StoreModule(dispatcher: Dispatcher) {
  val latestEvaluationStore: LatestEvaluationStore by lazy {
    LatestEvaluationStore(dispatcher)
  }
  val currentStore: CurrentStore by lazy {
    CurrentStore(dispatcher)
  }
  val eventStore: EventStore by lazy {
    EventStore(dispatcher)
  }
}

