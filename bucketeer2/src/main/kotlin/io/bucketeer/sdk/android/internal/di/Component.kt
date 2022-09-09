package io.bucketeer.sdk.android.internal.di

import io.bucketeer.sdk.android.internal.evaluation.EvaluationInteractor

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
}
