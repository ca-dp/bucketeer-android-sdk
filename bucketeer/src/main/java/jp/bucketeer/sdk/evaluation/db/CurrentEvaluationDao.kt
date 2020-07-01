package jp.bucketeer.sdk.evaluation.db

import bucketeer.feature.EvaluationOuterClass

internal interface CurrentEvaluationDao {
  fun upsertEvaluation(evaluation: EvaluationOuterClass.Evaluation)
  fun deleteNotIn(userId: String, featureIds: List<String>)
  fun getEvaluations(userId: String): List<EvaluationOuterClass.Evaluation>
}
