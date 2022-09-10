package jp.bucketeer.sdk.evaluation.db

import java.util.Arrays

internal data class LatestEvaluationEntity(
  val userId: String,
  val evaluation: ByteArray,
) {
  companion object {
    const val TABLE_NAME = "latest_evaluation"
    const val COLUMN_USER_ID = "user_id"
    const val COLUMN_FEATURE_ID = "feature_id"
    const val COLUMN_EVALUATION = "evaluation"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as LatestEvaluationEntity

    if (userId != other.userId) return false
    if (!Arrays.equals(evaluation, other.evaluation)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = userId.hashCode()
    result = 31 * result + Arrays.hashCode(evaluation)
    return result
  }
}
