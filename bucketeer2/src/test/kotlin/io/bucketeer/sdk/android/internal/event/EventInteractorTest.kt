package io.bucketeer.sdk.android.internal.event

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import io.bucketeer.sdk.android.BKTConfig
import io.bucketeer.sdk.android.internal.Clock
import io.bucketeer.sdk.android.internal.ClockImpl
import io.bucketeer.sdk.android.internal.IdGenerator
import io.bucketeer.sdk.android.internal.IdGeneratorImpl
import io.bucketeer.sdk.android.internal.di.Component
import io.bucketeer.sdk.android.internal.di.DataModule
import io.bucketeer.sdk.android.internal.di.InteractorModule
import io.bucketeer.sdk.android.internal.model.Event
import io.bucketeer.sdk.android.internal.model.EventData
import io.bucketeer.sdk.android.internal.model.EventType
import io.bucketeer.sdk.android.internal.model.Reason
import io.bucketeer.sdk.android.internal.model.ReasonType
import io.bucketeer.sdk.android.internal.model.SourceID
import io.bucketeer.sdk.android.mocks.evaluation1
import io.bucketeer.sdk.android.mocks.user1
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EventInteractorTest {
  private lateinit var server: MockWebServer

  private lateinit var component: Component
  private lateinit var moshi: Moshi
  private lateinit var idGenerator: FakeIdGenerator
  private lateinit var clock: FakeClock

  private lateinit var interactor: EventInteractor

  @Before
  fun setup() {
    server = MockWebServer()

    component = Component(
      dataModule = TestDataModule(
        application = ApplicationProvider.getApplicationContext(),
        config = BKTConfig.builder()
          .endpoint(server.url("").toString())
          .apiKey("api_key_value")
          .featureTag("feature_tag_value")
          .build(),
      ),
      interactorModule = InteractorModule(),
    )

    interactor = component.eventInteractor

    moshi = component.dataModule.moshi
    idGenerator = component.dataModule.idGenerator as FakeIdGenerator
    clock = component.dataModule.clock as FakeClock
  }

  @After
  fun tearDown() {
    server.shutdown()
  }

  @Test
  fun trackEvaluationEvent() {
    val listener = FakeEventUpdateListener()

    interactor.setEventUpdateListener(listener)

    interactor.trackEvaluationEvent("feature_tag_value", user1, evaluation1)

    assertThat(listener.calls).hasSize(1)
    assertThat(listener.calls[0]).hasSize(1)

    assertThat(idGenerator.calls).hasSize(1)
    assertThat(clock.currentTimeSecondsCalls).hasSize(1)

    val event = listener.calls[0][0]

    assertThat(event).isEqualTo(
      Event(
        id = idGenerator.calls[0],
        type = EventType.EVALUATION,
        event = EventData.EvaluationEvent(
          timestamp = clock.currentTimeSecondsCalls[0],
          feature_id = evaluation1.feature_id,
          feature_version = evaluation1.feature_version,
          user_id = user1.id,
          variation_id = evaluation1.variation_id,
          user = user1,
          reason = evaluation1.reason,
          tag = "feature_tag_value",
          source_id = SourceID.ANDROID
        )
      )
    )
  }
  
  @Test
  fun trackDefaultEvaluationEvent() {
    val listener = FakeEventUpdateListener()

    interactor.setEventUpdateListener(listener)

    interactor.trackDefaultEvaluationEvent("feature_tag_value", user1, "feature_id_value")

    assertThat(listener.calls).hasSize(1)
    assertThat(listener.calls[0]).hasSize(1)

    assertThat(idGenerator.calls).hasSize(1)
    assertThat(clock.currentTimeSecondsCalls).hasSize(1)

    val event = listener.calls[0][0]

    assertThat(event).isEqualTo(
      Event(
        id = idGenerator.calls[0],
        type = EventType.EVALUATION,
        event = EventData.EvaluationEvent(
          timestamp = clock.currentTimeSecondsCalls[0],
          feature_id = "feature_id_value",
          user_id = user1.id,
          user = user1,
          reason = Reason(ReasonType.CLIENT),
          tag = "feature_tag_value",
          source_id = SourceID.ANDROID
        )
      )
    )
  }

  @Ignore
  @Test
  fun trackGoalEvent() {

  }

  @Ignore
  @Test
  fun trackFetchEvaluationsSuccess() {

  }

  @Ignore
  @Test
  fun trackFetchEvaluationsFailure() {

  }

  @Ignore
  @Test
  fun `sendEvents - success`() {

  }

  @Ignore
  @Test
  fun `sendEvents - failure`() {

  }

  @Ignore
  @Test
  fun `sendEvents - current is empty`() {

  }

  @Ignore
  @Test
  fun `sendEvents - current cache is less than threshold`() {

  }

  @Ignore
  @Test
  fun refreshCache() {

  }
}

private class FakeEventUpdateListener() : EventInteractor.EventUpdateListener {

  val calls = mutableListOf<List<Event>>()

  override fun onUpdate(events: List<Event>) {
    calls.add(events)
  }
}

private class FakeIdGenerator : IdGenerator {
  val calls = mutableListOf<String>()

  private val impl = IdGeneratorImpl()

  override fun newId(): String {
    return impl.newId()
      .also { calls.add(it) }
  }
}

private class FakeClock : Clock {

  val currentTimeMillisCalls = mutableListOf<Long>()

  val currentTimeSecondsCalls = mutableListOf<Long>()

  private val impl = ClockImpl()

  override fun currentTimeMillis(): Long {
    return impl.currentTimeMillis()
      .also { currentTimeMillisCalls.add(it) }
  }

  override fun currentTimeSeconds(): Long {
    return impl.currentTimeSeconds()
      .also { currentTimeSecondsCalls.add(it) }
  }
}

private class TestDataModule(
  application: Application,
  config: BKTConfig
) : DataModule(application, config) {

  override val clock: Clock by lazy { FakeClock() }

  override val idGenerator: IdGenerator by lazy { FakeIdGenerator() }
}
