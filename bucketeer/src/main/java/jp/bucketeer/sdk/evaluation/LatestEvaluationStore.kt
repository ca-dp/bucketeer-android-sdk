package jp.bucketeer.sdk.evaluation

import bucketeer.feature.EvaluationOuterClass
import jp.bucketeer.sdk.dispatcher.Dispatcher
import jp.bucketeer.sdk.evaluation.dto.LatestEvaluationChangedAction
import jp.bucketeer.sdk.evaluation.dto.RefreshManuallyStateChangedAction
import jp.bucketeer.sdk.util.ObservableField

internal class LatestEvaluationStore(val dispatcher: Dispatcher) {
  val latestEvaluations: ObservableField<MutableMap<String, List<EvaluationOuterClass.Evaluation>>> =
    ObservableField(mutableMapOf())
  val refreshManuallyState: ObservableField<RefreshManuallyStateChangedAction.State> =
    ObservableField(RefreshManuallyStateChangedAction.State.Loading)

  init {
    dispatcher.addObserver { _, action ->
      when (action) {
        is LatestEvaluationChangedAction -> {
          latestEvaluations.value[action.user.id] = action.latestEvaluation
          latestEvaluations.value = latestEvaluations.value
        }
        is RefreshManuallyStateChangedAction -> {
          refreshManuallyState.value = action.state
        }
      }
    }
  }
}
