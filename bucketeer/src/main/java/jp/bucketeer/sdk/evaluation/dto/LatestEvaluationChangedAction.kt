package jp.bucketeer.sdk.evaluation.dto

import bucketeer.feature.EvaluationOuterClass
import bucketeer.user.UserOuterClass

internal data class LatestEvaluationChangedAction(
  val user: UserOuterClass.User,
  val latestEvaluation: List<EvaluationOuterClass.Evaluation>
)
