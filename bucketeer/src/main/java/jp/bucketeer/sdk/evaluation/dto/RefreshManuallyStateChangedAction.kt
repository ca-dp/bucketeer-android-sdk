package jp.bucketeer.sdk.evaluation.dto

import jp.bucketeer.sdk.BucketeerException

internal data class RefreshManuallyStateChangedAction(val state: State) {
  sealed class State {
    object Loading : State()
    object Loaded : State()
    class Error(val exception: BucketeerException) : State()
  }
}
