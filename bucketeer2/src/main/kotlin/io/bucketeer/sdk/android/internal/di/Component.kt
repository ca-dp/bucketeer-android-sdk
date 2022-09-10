package io.bucketeer.sdk.android.internal.di

import io.bucketeer.sdk.android.internal.evaluation.EvaluationInteractor
import io.bucketeer.sdk.android.internal.event.EventInteractor

internal class Component(
  val dataModule: DataModule,
  val interactorModule: InteractorModule,
) {
  val evaluationInteractor: EvaluationInteractor by lazy {
    interactorModule.evaluationInteractor(
      apiClient = dataModule.apiClient,
      evaluationDao = dataModule.evaluationDao,
      sharedPreferences = dataModule.sharedPreferences,
    )
  }

  val eventInteractor: EventInteractor by lazy {
    interactorModule.eventInteractor(
      eventsMaxBatchQueueCount = dataModule.config.eventsMaxBatchQueueCount,
      apiClient = dataModule.apiClient,
      eventDao = dataModule.eventDao,
      clock = dataModule.clock,
      idGenerator = dataModule.idGenerator
    )
  }
}
