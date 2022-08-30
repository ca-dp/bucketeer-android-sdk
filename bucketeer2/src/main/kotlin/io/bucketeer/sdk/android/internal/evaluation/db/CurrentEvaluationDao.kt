package io.bucketeer.sdk.android.internal.evaluation.db

import io.bucketeer.sdk.android.internal.model.Evaluation

internal interface CurrentEvaluationDao {
  fun upsertEvaluation(evaluation: Evaluation)
  fun deleteNotIn(userId: String, featureIds: List<String>)
  fun getEvaluations(userId: String): List<Evaluation>
}
