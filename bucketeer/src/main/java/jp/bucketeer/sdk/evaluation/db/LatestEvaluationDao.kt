package jp.bucketeer.sdk.evaluation.db

import bucketeer.feature.EvaluationOuterClass
import bucketeer.user.UserOuterClass

internal interface LatestEvaluationDao {
  fun put(user: UserOuterClass.User, list: List<EvaluationOuterClass.Evaluation>)
  fun get(user: UserOuterClass.User): List<EvaluationOuterClass.Evaluation>
  fun deleteAllAndInsert(
    user: UserOuterClass.User,
    list: List<EvaluationOuterClass.Evaluation>,
  ): Boolean
}
