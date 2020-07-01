package jp.bucketeer.sdk.evaluation

import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import jp.bucketeer.sdk.Api
import jp.bucketeer.sdk.dispatcher.Dispatcher
import jp.bucketeer.sdk.evaluation.db.CurrentEvaluationDao
import jp.bucketeer.sdk.evaluation.db.LatestEvaluationDao
import jp.bucketeer.sdk.evaluation.dto.CurrentEvaluationListDataChangedAction
import jp.bucketeer.sdk.evaluation.dto.LatestEvaluationChangedAction
import jp.bucketeer.sdk.evaluation1
import jp.bucketeer.sdk.responseFull
import jp.bucketeer.sdk.responsePartial
import jp.bucketeer.sdk.toBucketeerException
import jp.bucketeer.sdk.user1
import jp.bucketeer.sdk.user1Evaluations
import jp.bucketeer.sdk.userEvaluationsId1
import jp.bucketeer.sdk.userEvaluationsId2
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LatestEvaluationActionCreatorTest {

  @Test fun refreshSameUserEvaluationsId() {
    val latestEvaluationDao: LatestEvaluationDao = mock()
    val currentEvaluationActionCreator: CurrentEvaluationDao = mock()
    val sharedPref: SharedPreferences = mock()
    val dispatcher: Dispatcher = mock()
    val api = mock<Api>()
    whenever(sharedPref.getString(any(), any())).thenReturn(userEvaluationsId1)
    val evaluationActionCreator = spy(LatestEvaluationActionCreator(
        dispatcher,
        api,
        latestEvaluationDao,
        currentEvaluationActionCreator,
        sharedPref
    ))
    whenever(api.fetchEvaluation(user1, userEvaluationsId1)).doReturn(
        Api.Result.Success(responseFull))

    verify(currentEvaluationActionCreator, never()).getEvaluations(any())
    verify(evaluationActionCreator, never()).updateUserEvaluationId(any())
    verify(evaluationActionCreator, never()).refreshLatestEvaluationFromDao(any())
    verify(latestEvaluationDao, never()).deleteAllAndInsert(any(), any())
    verify(currentEvaluationActionCreator, never()).deleteNotIn(any(), any())
    verify(dispatcher, never()).send(any())
  }

  @Test fun refresh_partialResponse() {
    val latestEvaluationDao: LatestEvaluationDao = mock()
    val currentEvaluationActionCreator: CurrentEvaluationDao = mock()
    val sharedPref: SharedPreferences = mock()
    val dispatcher: Dispatcher = mock()
    val api = mock<Api>()
    whenever(sharedPref.getString(any(), any())).thenReturn(userEvaluationsId2)
    val evaluationActionCreator = spy(LatestEvaluationActionCreator(
        dispatcher,
        api,
        latestEvaluationDao,
        currentEvaluationActionCreator,
        sharedPref
    ))
    val dbEvaluations = listOf(evaluation1)
    doNothing().whenever(evaluationActionCreator).updateUserEvaluationId(any())
    doNothing().whenever(evaluationActionCreator).refreshLatestEvaluationFromDao(any())
    whenever(latestEvaluationDao.get(user1))
        .doReturn(
            dbEvaluations
        )
    whenever(api.fetchEvaluation(user1, userEvaluationsId2)).doReturn(
        Api.Result.Success(responsePartial))

    evaluationActionCreator.refreshLatestEvaluationFromApi(user1)

    verify(evaluationActionCreator, never()).updateUserEvaluationId(any())
    verify(evaluationActionCreator).refreshLatestEvaluationFromDao(any())
    verify(latestEvaluationDao, never()).deleteAllAndInsert(user1,
        responsePartial.evaluations.evaluationsList)
    verify(latestEvaluationDao).put(user1, responsePartial.evaluations.evaluationsList)
    verify(currentEvaluationActionCreator, never()).deleteNotIn(any(), any())
    verify(dispatcher).send(LatestEvaluationChangedAction(user1, dbEvaluations))
    verifyNoMoreInteractions(dispatcher)
  }

  @Test fun refresh_fullResponse() {
    val latestEvaluationDao: LatestEvaluationDao = mock()
    val currentEvaluationActionCreator: CurrentEvaluationDao = mock()
    val sharedPref: SharedPreferences = mock()
    val dispatcher: Dispatcher = mock()
    val api = mock<Api>()
    whenever(sharedPref.getString(any(), any())).thenReturn(userEvaluationsId2)
    val evaluationActionCreator = spy(LatestEvaluationActionCreator(
        dispatcher,
        api,
        latestEvaluationDao,
        currentEvaluationActionCreator,
        sharedPref
    ))
    whenever(latestEvaluationDao.deleteAllAndInsert(any(), any())).thenReturn(true)
    doNothing().whenever(evaluationActionCreator).updateUserEvaluationId(any())
    doNothing().whenever(evaluationActionCreator).refreshLatestEvaluationFromDao(any())
    whenever(api.fetchEvaluation(user1, userEvaluationsId2)).doReturn(
        Api.Result.Success(responseFull))
    whenever(currentEvaluationActionCreator.getEvaluations(user1.id))
        .doReturn(listOf(user1Evaluations.evaluationsList))

    evaluationActionCreator.refreshLatestEvaluationFromApi(user1)

    verify(evaluationActionCreator).updateUserEvaluationId(any())
    verify(evaluationActionCreator).refreshLatestEvaluationFromDao(any())
    verify(latestEvaluationDao).deleteAllAndInsert(user1, responseFull.evaluations.evaluationsList)
    verify(currentEvaluationActionCreator)
        .deleteNotIn(user1.id, listOf("test-feature-1", "test-feature-2"))
    verify(currentEvaluationActionCreator).getEvaluations(user1.id)
    verify(dispatcher).send(
        LatestEvaluationChangedAction(user1, responseFull.evaluations.evaluationsList))
    verify(dispatcher)
        .send(CurrentEvaluationListDataChangedAction("user id 1", user1Evaluations.evaluationsList))
    verifyNoMoreInteractions(dispatcher)
  }

  @Test fun refresh_fail_to_save() {
    val latestEvaluationDao: LatestEvaluationDao = mock()
    val currentEvaluationActionCreator: CurrentEvaluationDao = mock()
    val sharedPref: SharedPreferences = mock()
    val dispatcher: Dispatcher = mock()
    val api = mock<Api>()
    whenever(sharedPref.getString(any(), any())).thenReturn(userEvaluationsId1)
    val evaluationActionCreator = spy(LatestEvaluationActionCreator(
        dispatcher,
        api,
        latestEvaluationDao,
        currentEvaluationActionCreator,
        sharedPref
    ))
    whenever(latestEvaluationDao.deleteAllAndInsert(any(), any())).thenReturn(false)
    whenever(api.fetchEvaluation(user1, userEvaluationsId2)).doReturn(
        Api.Result.Success(responseFull))

    verify(evaluationActionCreator, never()).updateUserEvaluationId(any())
    verify(evaluationActionCreator, never()).refreshLatestEvaluationFromDao(any())
    verify(latestEvaluationDao, never()).put(any(), any())
    verify(currentEvaluationActionCreator, never()).deleteNotIn(any(), any())
    verify(dispatcher, never()).send(any<LatestEvaluationChangedAction>())
    verifyNoMoreInteractions(dispatcher)
  }

  @Test fun refresh_fail() {
    val latestEvaluationDao: LatestEvaluationDao = mock()
    val currentEvaluationActionCreator: CurrentEvaluationDao = mock()
    val sharedPref: SharedPreferences = mock()
    val dispatcher: Dispatcher = mock()
    val api = mock<Api>()
    whenever(sharedPref.getString(any(), any())).thenReturn(userEvaluationsId1)
    val evaluationActionCreator = spy(LatestEvaluationActionCreator(
        dispatcher,
        api,
        latestEvaluationDao,
        currentEvaluationActionCreator,
        sharedPref
    ))
    doNothing().whenever(evaluationActionCreator).refreshLatestEvaluationFromDao(any())
    whenever(api.fetchEvaluation(user1, userEvaluationsId1)).doReturn(
        Api.Result.Fail(RuntimeException().toBucketeerException()))

    evaluationActionCreator.refreshLatestEvaluationFromApi(user1)

    verify(evaluationActionCreator, never()).updateUserEvaluationId(any())
    verify(evaluationActionCreator).refreshLatestEvaluationFromDao(any())
    verify(latestEvaluationDao, never()).put(any(), any())
    verify(currentEvaluationActionCreator, never()).deleteNotIn(any(), any())
    verify(dispatcher, never()).send(any<LatestEvaluationChangedAction>())
    verifyNoMoreInteractions(dispatcher)
  }
}
