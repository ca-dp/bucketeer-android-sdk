package jp.bucketeer.sdk

import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import jp.bucketeer.sdk.evaluation.ClientInteractorActionCreator
import jp.bucketeer.sdk.evaluation.CurrentStore
import jp.bucketeer.sdk.evaluation.LatestEvaluationActionCreator
import jp.bucketeer.sdk.evaluation.LatestEvaluationStore
import jp.bucketeer.sdk.evaluation.dto.RefreshManuallyStateChangedAction
import jp.bucketeer.sdk.events.EventActionCreator
import jp.bucketeer.sdk.user.UserHolder
import jp.bucketeer.sdk.util.ObservableField
import org.junit.Assert.assertEquals
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper
import java.util.Date
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class ClientInteractorTest {
  private val clientInteractorActionCreator: ClientInteractorActionCreator = mock()
  private val eventActionCreator: EventActionCreator = mock()
  private val latestEvaluationActionCreator: LatestEvaluationActionCreator = mock()
  private val firstRefreshState = ObservableField<RefreshManuallyStateChangedAction.State>(
      RefreshManuallyStateChangedAction.State.Loading)
  private val latestEvaluationStore: LatestEvaluationStore = mock<LatestEvaluationStore>().also {
    whenever(it.refreshManuallyState).thenReturn(firstRefreshState)
  }
  private val currentStore: CurrentStore = mock()
  private val userHolder: UserHolder.UpdatableUserHolder = spy(
      UserHolder.UpdatableUserHolder().apply { updateUser(user1) }
  )

  private lateinit var clientInteractor: ClientInteractor

  @Before fun setUp() {
    clientInteractor = ClientInteractor(
        clientInteractorActionCreator,
        eventActionCreator,
        latestEvaluationActionCreator,
        latestEvaluationStore,
        currentStore,
        userHolder
    )
  }

  @Test fun refreshCurrentEvaluation_whenUserChanged() {
    clientInteractor.setUser(user2)
    clientInteractor.fetchUserEvaluations()

    verify(clientInteractorActionCreator).refreshCurrentEvaluation(user2.id)
  }

  @Test fun setUser() {
    clientInteractor.setUser(user2)

    verify(userHolder).updateUser(user2)
  }

  @Test fun setUser_WaitCallback() {
    clientInteractor.setUser(user2)
    firstRefreshState.value = RefreshManuallyStateChangedAction.State.Loaded
    ShadowLooper.runUiThreadTasks()

    verify(userHolder).updateUser(user2)
  }

  @Test fun setUser_WaitCallbackWhenError() {
    val expectedException = RuntimeException().toBucketeerException()

    clientInteractor.setUser(user2)
    firstRefreshState.value = RefreshManuallyStateChangedAction.State.Error(expectedException)
    ShadowLooper.runUiThreadTasks()

    verify(userHolder).updateUser(user2)
  }
}

@RunWith(RobolectricTestRunner::class)
class ClientInteractorTestGetEvaluation {
  private val clientInteractorActionCreator: ClientInteractorActionCreator = mock()
  private val eventActionCreator: EventActionCreator = mock()
  private val latestEvaluationActionCreator: LatestEvaluationActionCreator = mock()
  private val firstRefreshState = ObservableField<RefreshManuallyStateChangedAction.State>(
      RefreshManuallyStateChangedAction.State.Loading)
  private val latestEvaluationStore: LatestEvaluationStore = mock<LatestEvaluationStore>().also {
    whenever(it.refreshManuallyState).thenReturn(firstRefreshState)
  }
  private val currentStore: CurrentStore = mock()
  private val userHolder: UserHolder.UpdatableUserHolder = spy(
      UserHolder.UpdatableUserHolder().apply { updateUser(user1) }
  )

  private lateinit var clientInteractor: ClientInteractor

  @Before fun setUp() {
    clientInteractor = spy(ClientInteractor(
        clientInteractorActionCreator,
        eventActionCreator,
        latestEvaluationActionCreator,
        latestEvaluationStore,
        currentStore,
        userHolder
    ))
  }

  @Test fun getLatestEvaluation_findVariationThenReturnEvaluation() {
    whenever(latestEvaluationStore.latestEvaluations)
        .thenReturn(ObservableField(mutableMapOf(user1.id to listOf(evaluation2))))
    whenever(userHolder.userId).thenReturn(user1.id)

    val variationValue = clientInteractor.getLatestEvaluation("test-feature-2")

    verify(latestEvaluationStore).latestEvaluations
    assertEquals(evaluation2, variationValue)
  }

  @Test fun getLatestEvaluation_notFoundVariationThenReturnNull() {
    whenever(latestEvaluationStore.latestEvaluations)
        .thenReturn(ObservableField(mutableMapOf(user1.id to listOf(evaluation2))))

    val variationValue = clientInteractor.getLatestEvaluation("unknownId")

    verify(latestEvaluationStore).latestEvaluations
    assertEquals(null, variationValue)
  }

  @Test fun pushEvaluationEvent_createPushEvaluationAction() {
    userHolder.updateUser(user1)

    clientInteractor.pushEvaluationEvent(user1, evaluation1, 1234_5678)

    verify(eventActionCreator).pushEvaluationEvent(1234_5678, evaluation1, user1)
  }
}

@RunWith(RobolectricTestRunner::class)
class ClientInteractorTestTrack {
  private val clientInteractorActionCreator: ClientInteractorActionCreator = mock()
  private val eventActionCreator: EventActionCreator = mock()
  private val latestEvaluationActionCreator: LatestEvaluationActionCreator = mock()
  private val firstRefreshState = ObservableField<RefreshManuallyStateChangedAction.State>(
      RefreshManuallyStateChangedAction.State.Loading)
  private val latestEvaluationStore: LatestEvaluationStore = mock<LatestEvaluationStore>().also {
    whenever(it.refreshManuallyState).thenReturn(firstRefreshState)
  }
  private val currentStore: CurrentStore = mock()
  private val userHolder: UserHolder.UpdatableUserHolder = spy(
      UserHolder.UpdatableUserHolder().apply { updateUser(user1) }
  )

  private lateinit var clientInteractor: ClientInteractor

  @Before fun setUp() {
    clientInteractor = spy(ClientInteractor(
        clientInteractorActionCreator,
        eventActionCreator,
        latestEvaluationActionCreator,
        latestEvaluationStore,
        currentStore,
        userHolder
    ))
  }

  @Test fun track_callGetCurrentStateAndPushGoalEvent() {
    whenever(userHolder.userId).thenReturn("user id 1")
    doReturn(1234L).whenever(clientInteractor).getTimestamp()
    doReturn(listOf(evaluation1)).whenever(clientInteractor).getCurrentState("user id 1")
    doNothing().whenever(clientInteractor)
        .pushGoalEvent(1234, "goalId", user1, 2.0, listOf(evaluation1))

    clientInteractor.track(user1, "goalId", 2.0)

    verify(clientInteractor).getCurrentState("user id 1")
    verify(clientInteractor).pushGoalEvent(1234, "goalId", user1, 2.0, listOf(evaluation1))
  }

  @Test fun getCurrentState_returnCurrentStoreValue() {
    whenever(currentStore.currentEvaluations)
        .thenReturn(ObservableField(mapOf("user id 1" to listOf(evaluation1))))

    val currentState = clientInteractor.getCurrentState("user id 1")

    currentState shouldEqual listOf(evaluation1)
  }

  @Test fun pushGoalEvent_callEventAction() {
    clientInteractor
        .pushGoalEvent(1234, "goalId", user1, 100.0, listOf(evaluation1))

    verify(eventActionCreator).pushGoalEvent(1234, "goalId", 100.0, user1, listOf(evaluation1))
  }

  @Test fun refreshLatestEvaluation_whenUserChanged() {
    clientInteractor.setUser(user2)
    clientInteractor.fetchUserEvaluations()

    verify(latestEvaluationActionCreator).refreshLatestEvaluationManuallyFromApi(user2)
  }

  @Test fun getTimestamp_returnInSeconds() {
    val minDate = Date(0)
    val maxDate = Date(9_999_999_999.toMills())

    val date = Date(clientInteractor.getTimestamp().toMills())

    date.after(minDate) shouldEqual true
    date.before(maxDate) shouldEqual true
  }

  private fun Long.toMills() = TimeUnit.SECONDS.toMillis(this)
}
