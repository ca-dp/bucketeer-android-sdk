package jp.bucketeer.sdk.evaluation.dto

import bucketeer.feature.EvaluationOuterClass

internal data class CurrentEvaluationListDataChangedAction(
  val userId: String,
  val evaluations: List<EvaluationOuterClass.Evaluation>,
)
