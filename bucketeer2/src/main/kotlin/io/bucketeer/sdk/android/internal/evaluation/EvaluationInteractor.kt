package io.bucketeer.sdk.android.internal.evaluation

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import io.bucketeer.sdk.android.internal.Constants
import io.bucketeer.sdk.android.internal.evaluation.db.EvaluationDao
import io.bucketeer.sdk.android.internal.logd
import io.bucketeer.sdk.android.internal.loge
import io.bucketeer.sdk.android.internal.model.Evaluation
import io.bucketeer.sdk.android.internal.model.User
import io.bucketeer.sdk.android.internal.remote.ApiClient
import io.bucketeer.sdk.android.internal.remote.GetEvaluationsResult
import java.util.concurrent.Executor

internal class EvaluationInteractor(
  private val apiClient: ApiClient,
  private val evaluationDao: EvaluationDao,
  private val sharedPrefs: SharedPreferences,
  private val executor: Executor,
) {
  // key: userId
  @VisibleForTesting
  internal val latestEvaluations = mutableMapOf<String, List<Evaluation>>()

  @VisibleForTesting
  internal var currentEvaluationsId: String
    get() = sharedPrefs.getString(Constants.PREFERENCE_KEY_USER_EVALUATION_ID, "") ?: ""
    @SuppressLint("ApplySharedPref")
    set(value) {
      sharedPrefs.edit()
        .putString(Constants.PREFERENCE_KEY_USER_EVALUATION_ID, value)
        .commit()
    }

  @Suppress("MoveVariableDeclarationIntoWhen")
  fun fetch(user: User, timeoutMillis: Long?): GetEvaluationsResult {
    val currentEvaluationsId = this.currentEvaluationsId

    val result = apiClient.getEvaluations(user, currentEvaluationsId, timeoutMillis)

    when (result) {
      is GetEvaluationsResult.Success -> {
        val response = result.value.data
        val newEvaluationsId = response.user_evaluations_id
        if (currentEvaluationsId == newEvaluationsId) {
          logd { "Nothing to sync" }
          return result
        }

        val newEvaluations = response.evaluations.evaluations

        val success = evaluationDao.deleteAllAndInsert(user.id, newEvaluations)
        if (!success) {
          loge { "Failed to update latest evaluations" }
          return result
        }

        this.currentEvaluationsId = newEvaluationsId

        latestEvaluations[user.id] = newEvaluations
      }
      is GetEvaluationsResult.Failure -> {
        logd(result.error) { "ApiError: ${result.error.message}" }
      }
    }
    return result
  }

  fun getLatest(userId: String, featureId: String): Evaluation? {
    val evaluations = latestEvaluations[userId] ?: emptyList()
    return evaluations.firstOrNull { it.feature_id == featureId }
  }
}