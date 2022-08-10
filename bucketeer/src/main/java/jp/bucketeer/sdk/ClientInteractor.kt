package jp.bucketeer.sdk

import android.os.Handler
import android.os.Looper
import androidx.annotation.VisibleForTesting
import bucketeer.feature.EvaluationOuterClass
import bucketeer.user.UserOuterClass
import jp.bucketeer.sdk.evaluation.ClientInteractorActionCreator
import jp.bucketeer.sdk.evaluation.CurrentStore
import jp.bucketeer.sdk.evaluation.LatestEvaluationActionCreator
import jp.bucketeer.sdk.evaluation.LatestEvaluationStore
import jp.bucketeer.sdk.evaluation.dto.RefreshManuallyStateChangedAction
import jp.bucketeer.sdk.events.EventActionCreator
import jp.bucketeer.sdk.user.UserHolder

internal class ClientInteractor(
  private val clientInteractorActionCreator: ClientInteractorActionCreator,
  private val eventActionCreator: EventActionCreator,
  private val latestEvaluationActionCreator: LatestEvaluationActionCreator,
  private val latestEvaluationStore: LatestEvaluationStore,
  private val currentStore: CurrentStore,
  private val updatableUserHolder: UserHolder.UpdatableUserHolder = UserHolder.UpdatableUserHolder()
) {
  val userHolder: UserHolder = updatableUserHolder
  private var fetchUserEvaluationsCallback: Bucketeer.FetchUserEvaluationsCallback? = null
  private val handler = Handler(Looper.getMainLooper())
  val isUserSet: Boolean get() = userHolder.hasUser

  init {
    latestEvaluationStore.refreshManuallyState.addObserver { state ->
      handler.post {
        when (state) {
          RefreshManuallyStateChangedAction.State.Loaded ->
            fetchUserEvaluationsCallback?.onSuccess()
          is RefreshManuallyStateChangedAction.State.Error ->
            fetchUserEvaluationsCallback?.onError(state.exception)
          RefreshManuallyStateChangedAction.State.Loading -> {
            // do nothing
          }
        }
      }
    }
  }

  fun setUser(
    user: UserOuterClass.User
  ) {
    updatableUserHolder.updateUser(user)
  }

  fun fetchUserEvaluations(
    fetchUserEvaluationsCallback: Bucketeer.FetchUserEvaluationsCallback? = null
  ) {
    this.fetchUserEvaluationsCallback = fetchUserEvaluationsCallback
    refreshManuallyFromApi()
    clientInteractorActionCreator.refreshCurrentEvaluation(updatableUserHolder.userId)
  }

  fun getLatestEvaluation(featureId: String): EvaluationOuterClass.Evaluation? {
    val evaluations = latestEvaluationStore.latestEvaluations.value
    val evaluation: EvaluationOuterClass.Evaluation? =
      evaluations[userHolder.userId]?.firstOrNull { it.featureId == featureId }

    evaluation ?: return null
    return evaluation
  }

  fun saveCurrentEvaluationEvent(userId: String, evaluation: EvaluationOuterClass.Evaluation) {
    clientInteractorActionCreator.saveCurrentEvaluation(userId, evaluation)
  }

  fun pushEvaluationEvent(
    user: UserOuterClass.User,
    evaluation: EvaluationOuterClass.Evaluation,
    timestamp: Long = getTimestamp()
  ) {
    eventActionCreator.pushEvaluationEvent(timestamp, evaluation, user)
  }

  fun pushDefaultEvaluationEvent(
    user: UserOuterClass.User,
    featureId: String,
    timestamp: Long = getTimestamp()
  ) {
    eventActionCreator.pushDefaultEvaluationEvent(timestamp, user, featureId)
  }

  fun track(user: UserOuterClass.User, goalId: String, value: Double) {
    val currentStates = getCurrentState(user.id)
    pushGoalEvent(getTimestamp(), goalId, user, value, currentStates)
  }

  @VisibleForTesting
  fun getCurrentState(userId: String): List<EvaluationOuterClass.Evaluation> {
    return currentStore.currentEvaluations.value[userId] ?: listOf()
  }

  @VisibleForTesting
  fun pushGoalEvent(
    timestamp: Long,
    goalId: String,
    user: UserOuterClass.User,
    value: Double,
    currentEvaluations: List<EvaluationOuterClass.Evaluation>
  ) {
    eventActionCreator.pushGoalEvent(timestamp, goalId, value, user, currentEvaluations)
  }

  @VisibleForTesting
  fun getTimestamp(): Long = System.currentTimeMillis() / 1000

  private fun refreshManuallyFromApi() {
    latestEvaluationActionCreator.refreshLatestEvaluationManuallyFromApi(userHolder.user)
  }
}
