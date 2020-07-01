package jp.bucketeer.sdk.di

import jp.bucketeer.sdk.dispatcher.Dispatcher
import jp.bucketeer.sdk.evaluation.ClientInteractorActionCreator
import jp.bucketeer.sdk.evaluation.LatestEvaluationActionCreator
import jp.bucketeer.sdk.events.EventActionCreator

internal class ActionCreatorModule(
    dispatcher: Dispatcher,
    dataModule: DataModule
) {
  val latestEvaluationActionCreator: LatestEvaluationActionCreator = LatestEvaluationActionCreator(
      dispatcher,
      dataModule.api,
      dataModule.latestEvaluationDao,
      dataModule.currentEvaluationDao,
      dataModule.sharedPreferences
  )
  val eventActionCreator: EventActionCreator = EventActionCreator(
      dataModule.api,
      dispatcher,
      dataModule.eventDao
  )
  val clientInteractorActionCreator: ClientInteractorActionCreator = ClientInteractorActionCreator(
      dispatcher,
      dataModule.api,
      dataModule.currentEvaluationDao
  )
}

