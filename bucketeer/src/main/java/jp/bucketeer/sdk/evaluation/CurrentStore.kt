package jp.bucketeer.sdk.evaluation

import bucketeer.feature.EvaluationOuterClass
import jp.bucketeer.sdk.dispatcher.Dispatcher
import jp.bucketeer.sdk.evaluation.dto.CurrentEvaluationListDataChangedAction
import jp.bucketeer.sdk.util.ObservableField

internal class CurrentStore(dispatcher: Dispatcher) {
  val currentEvaluations: ObservableField<Map<String, List<EvaluationOuterClass.Evaluation>>> =
      ObservableField(mapOf())

  init {
    dispatcher.addObserver { _, arg ->
      when (arg) {
        is CurrentEvaluationListDataChangedAction -> {
          val map = currentEvaluations.value.toMutableMap()
          map[arg.userId] = arg.evaluations
          currentEvaluations.value = map
        }
      }
    }
  }
}
