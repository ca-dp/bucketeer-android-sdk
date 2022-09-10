package jp.bucketeer.sdk.evaluation

import bucketeer.feature.EvaluationOuterClass
import jp.bucketeer.sdk.Api
import jp.bucketeer.sdk.dispatcher.Dispatcher
import jp.bucketeer.sdk.evaluation.db.CurrentEvaluationDao
import jp.bucketeer.sdk.evaluation.dto.CurrentEvaluationListDataChangedAction

internal class ClientInteractorActionCreator(
  private val dispatcher: Dispatcher,
  val api: Api,
  private val currentEvaluationDao: CurrentEvaluationDao,
) {

  fun saveCurrentEvaluation(currentUserId: String, evaluation: EvaluationOuterClass.Evaluation) {
    currentEvaluationDao.upsertEvaluation(evaluation)

    val userEvaluations = currentEvaluationDao.getEvaluations(currentUserId)
    dispatcher.send(CurrentEvaluationListDataChangedAction(currentUserId, userEvaluations))
  }

  fun refreshCurrentEvaluation(currentUserId: String) {
    val userEvaluations = currentEvaluationDao.getEvaluations(currentUserId)
    dispatcher.send(CurrentEvaluationListDataChangedAction(currentUserId, userEvaluations))
  }
}
