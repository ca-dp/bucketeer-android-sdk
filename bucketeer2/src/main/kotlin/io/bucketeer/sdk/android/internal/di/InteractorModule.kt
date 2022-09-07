package io.bucketeer.sdk.android.internal.di

import android.content.SharedPreferences
import io.bucketeer.sdk.android.internal.evaluation.EvaluationInteractor
import io.bucketeer.sdk.android.internal.evaluation.db.CurrentEvaluationDao
import io.bucketeer.sdk.android.internal.evaluation.db.LatestEvaluationDao
import io.bucketeer.sdk.android.internal.remote.ApiClient
import java.util.concurrent.Executor

internal class InteractorModule {
  fun evaluationInteractor(
    apiClient: ApiClient,
    currentEvaluationDao: CurrentEvaluationDao,
    latestEvaluationDao: LatestEvaluationDao,
    sharedPreferences: SharedPreferences,
    executor: Executor
  ): EvaluationInteractor {
    return EvaluationInteractor(
      apiClient = apiClient,
      currentEvaluationDao = currentEvaluationDao,
      latestEvaluationDao = latestEvaluationDao,
      sharedPrefs = sharedPreferences,
      executor = executor
    )
  }
}
