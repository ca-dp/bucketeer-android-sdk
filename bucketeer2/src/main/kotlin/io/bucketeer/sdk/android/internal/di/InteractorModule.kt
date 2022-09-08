package io.bucketeer.sdk.android.internal.di

import android.content.SharedPreferences
import io.bucketeer.sdk.android.internal.evaluation.EvaluationInteractor
import io.bucketeer.sdk.android.internal.evaluation.db.EvaluationDao
import io.bucketeer.sdk.android.internal.remote.ApiClient
import java.util.concurrent.Executor

internal class InteractorModule {
  fun evaluationInteractor(
    apiClient: ApiClient,
    evaluationDao: EvaluationDao,
    sharedPreferences: SharedPreferences,
    executor: Executor
  ): EvaluationInteractor {
    return EvaluationInteractor(
      apiClient = apiClient,
      evaluationDao = evaluationDao,
      sharedPrefs = sharedPreferences,
      executor = executor
    )
  }
}
