package jp.bucketeer.sdk.evaluation

import bucketeer.feature.EvaluationOuterClass
import jp.bucketeer.sdk.dispatcher.Dispatcher
import jp.bucketeer.sdk.evaluation.dto.LatestEvaluationChangedAction
import jp.bucketeer.sdk.user1
import jp.bucketeer.sdk.user1Evaluations
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class LatestEvaluationStoreTest {
  @Test
  fun latestEvaluation_observe() {
    val dispatcher = Dispatcher()
    val latestStore = LatestEvaluationStore(dispatcher)
    val observer: (MutableMap<String, List<EvaluationOuterClass.Evaluation>>) -> Unit = mock()
    latestStore.latestEvaluations.addObserver(observer)

    dispatcher.send(LatestEvaluationChangedAction(user1, user1Evaluations.evaluationsList))

    verify(observer).invoke(mutableMapOf(user1.id to user1Evaluations.evaluationsList))
    verifyNoMoreInteractions(observer)
  }
}
