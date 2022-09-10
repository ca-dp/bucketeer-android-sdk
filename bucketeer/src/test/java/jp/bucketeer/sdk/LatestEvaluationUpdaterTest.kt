package jp.bucketeer.sdk

import jp.bucketeer.sdk.evaluation.LatestEvaluationActionCreator
import jp.bucketeer.sdk.user.UserHolder
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import java.util.concurrent.Executors

class LatestEvaluationUpdaterTest {
  private val evaluationActionCreator = mock<LatestEvaluationActionCreator>()
  private val userHolder = UserHolder.UpdatableUserHolder().apply {
    updateUser(user1)
  }

  @Test
  fun pollEvaluation_refresh() {
    val latestEvaluationUpdater = createLatestEvaluationUpdaterWithSendingInterval(50)

    latestEvaluationUpdater.start()

    verify(evaluationActionCreator, timeout(90)).refreshLatestEvaluationFromApi(user1)
  }

  @Test
  fun pollEvaluation_refreshMultipleTimes() {
    val latestEvaluationUpdater = createLatestEvaluationUpdaterWithSendingInterval(40)

    latestEvaluationUpdater.start()

    verify(evaluationActionCreator, timeout(115).times(2)).refreshLatestEvaluationFromApi(user1)
  }

  private fun createLatestEvaluationUpdaterWithSendingInterval(
    logSendingIntervalMillis: Long,
  ): LatestEvaluationUpdater {
    return LatestEvaluationUpdater(
      logSendingIntervalMillis,
      evaluationActionCreator,
      userHolder,
      Executors.newSingleThreadScheduledExecutor(),
    )
  }
}
