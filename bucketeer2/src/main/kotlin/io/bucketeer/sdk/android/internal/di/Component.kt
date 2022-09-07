package io.bucketeer.sdk.android.internal.di

import io.bucketeer.sdk.android.internal.evaluation.EvaluationInteractor
import java.util.concurrent.ScheduledExecutorService

internal class Component(
  val dataModule: DataModule,
  val interactorModule: InteractorModule,
  val executor: ScheduledExecutorService
) {
  val evaluationInteractor: EvaluationInteractor by lazy {
    interactorModule.evaluationInteractor(
      apiClient = dataModule.apiClient,
      currentEvaluationDao = dataModule.currentEvaluationDao,
      latestEvaluationDao = dataModule.latestEvaluationDao,
      sharedPreferences = dataModule.sharedPreferences,
      executor = executor
    )
  }
}
