package jp.bucketeer.sdk.evaluation

import bucketeer.feature.EvaluationOuterClass
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import jp.bucketeer.sdk.dispatcher.Dispatcher
import jp.bucketeer.sdk.evaluation.dto.CurrentEvaluationListDataChangedAction
import jp.bucketeer.sdk.user1
import jp.bucketeer.sdk.user1Evaluations
import org.amshove.kluent.mock
import org.amshove.kluent.shouldEqual
import org.junit.Test

class CurrentStoreTest {

  @Test fun currentEvaluations_observe() {
    val dispatcher = Dispatcher()
    val currentStore = CurrentStore(dispatcher)
    val observer: (Map<String, List<EvaluationOuterClass.Evaluation>>) -> Unit = mock()

    currentStore.currentEvaluations.addObserver(observer)

    dispatcher.send(
        CurrentEvaluationListDataChangedAction(user1.id, user1Evaluations.evaluationsList))

    verify(observer).invoke(mapOf("user id 1" to user1Evaluations.evaluationsList))
    verifyNoMoreInteractions(observer)

    currentStore.currentEvaluations.value["user id 1"] shouldEqual user1Evaluations.evaluationsList
    currentStore.currentEvaluations.value["user id 2"] shouldEqual null
  }
}
