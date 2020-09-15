package jp.bucketeer.sdk.evaluation

import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import bucketeer.feature.EvaluationOuterClass
import bucketeer.user.UserOuterClass
import jp.bucketeer.sdk.Api
import jp.bucketeer.sdk.Constants
import jp.bucketeer.sdk.dispatcher.Dispatcher
import jp.bucketeer.sdk.evaluation.db.CurrentEvaluationDao
import jp.bucketeer.sdk.evaluation.db.LatestEvaluationDao
import jp.bucketeer.sdk.evaluation.dto.CurrentEvaluationListDataChangedAction
import jp.bucketeer.sdk.evaluation.dto.LatestEvaluationChangedAction
import jp.bucketeer.sdk.evaluation.dto.RefreshManuallyStateChangedAction
import jp.bucketeer.sdk.log.logd
import jp.bucketeer.sdk.log.loge

internal class LatestEvaluationActionCreator(
    private val dispatcher: Dispatcher,
    val api: Api,
    private val latestEvaluationDao: LatestEvaluationDao,
    private val currentEvaluationDao: CurrentEvaluationDao,
    private val sharePref: SharedPreferences
) {

  @VisibleForTesting
  var currentUserEvaluationsId: String = sharePref.getString(
      Constants.PREFERENCE_KEY_USER_EVALUATION_ID, "") ?: ""

  fun refreshLatestEvaluationManuallyFromApi(user: UserOuterClass.User) {
    dispatcher.send(
        RefreshManuallyStateChangedAction(RefreshManuallyStateChangedAction.State.Loading))
    val result = refreshLatestEvaluationFromApi(user)
    val state = when (result) {
      is Api.Result.Success -> RefreshManuallyStateChangedAction.State.Loaded
      is Api.Result.Fail -> RefreshManuallyStateChangedAction.State.Error(result.e)
    }
    dispatcher.send(RefreshManuallyStateChangedAction(state))
  }

  fun refreshLatestEvaluationFromApi(user: UserOuterClass.User): Api.Result<*> {
    refreshLatestEvaluationFromDao(user)
    val currentUserEvaluationId = this.currentUserEvaluationsId
    val result = api.fetchEvaluation(user, currentUserEvaluationId)
    when (result) {
      is Api.Result.Success -> {
        run {
          val response = result.value
          val userEvaluationsId = response.userEvaluationsId
          // Do nothing in case of there are no changes in the user evaluations
          if (currentUserEvaluationId == userEvaluationsId) {
            logd { "Nothing to sync" }
            return@run
          }
          val list = response.evaluations?.evaluationsList ?: listOf()
          when (response.state) {
            EvaluationOuterClass.UserEvaluations.State.FULL -> {
              if (!latestEvaluationDao.deleteAllAndInsert(user, list)) {
                loge { "Failed to update latest evaluations" }
                return@run
              }
              updateUserEvaluationId(userEvaluationsId)
              val featureIds = list.map { it.featureId }
              currentEvaluationDao.deleteNotIn(user.id, featureIds)
              val currentEvaluations = currentEvaluationDao.getEvaluations(user.id)

              dispatcher.send(LatestEvaluationChangedAction(user, list))
              dispatcher.send(CurrentEvaluationListDataChangedAction(user.id, currentEvaluations))
            }
            EvaluationOuterClass.UserEvaluations.State.PARTIAL -> {
              latestEvaluationDao.put(user, list)
              dispatcher.send(LatestEvaluationChangedAction(user, latestEvaluationDao.get(user)))
            }
            EvaluationOuterClass.UserEvaluations.State.QUEUED,
            EvaluationOuterClass.UserEvaluations.State.UNRECOGNIZED,
            null -> {
              // do nothing
            }
            else -> {
              // do nothing
            }
          }
        }
      }
      is Api.Result.Fail -> {
        logd(throwable = result.e) { "ApiError: ${result.e.message}" }
      }
    }
    return result
  }

  @VisibleForTesting
  fun updateUserEvaluationId(userEvaluationsId: String) {
    currentUserEvaluationsId = userEvaluationsId
    sharePref.edit().putString(
        Constants.PREFERENCE_KEY_USER_EVALUATION_ID,
        userEvaluationsId
    ).commit()
  }

  @VisibleForTesting
  fun refreshLatestEvaluationFromDao(user: UserOuterClass.User) {
    val latestEvaluationFromDao = latestEvaluationDao.get(user)
    dispatcher.send(LatestEvaluationChangedAction(user, latestEvaluationFromDao))
  }
}
