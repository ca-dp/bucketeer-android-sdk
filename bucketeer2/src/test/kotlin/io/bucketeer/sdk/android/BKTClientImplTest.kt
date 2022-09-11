package io.bucketeer.sdk.android

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import io.bucketeer.sdk.android.internal.di.DataModule
import io.bucketeer.sdk.android.internal.model.Evaluation
import io.bucketeer.sdk.android.internal.model.Event
import io.bucketeer.sdk.android.internal.model.EventData
import io.bucketeer.sdk.android.internal.model.EventType
import io.bucketeer.sdk.android.internal.model.MetricsEventData
import io.bucketeer.sdk.android.internal.model.MetricsEventType
import io.bucketeer.sdk.android.internal.model.response.GetEvaluationsDataResponse
import io.bucketeer.sdk.android.internal.model.response.GetEvaluationsResponse
import io.bucketeer.sdk.android.internal.user.toBKTUser
import io.bucketeer.sdk.android.mocks.user1
import io.bucketeer.sdk.android.mocks.user1Evaluations
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class BKTClientImplTest {
  private lateinit var server: MockWebServer

  private lateinit var config: BKTConfig

  private lateinit var moshi: Moshi

  @Before
  fun setup() {
    server = MockWebServer()

    config = BKTConfig.builder()
      .endpoint(server.url("").toString())
      .apiKey("api_key_value")
      .featureTag("feature_tag_value")
      .build()

    moshi = DataModule.createMoshi()
  }

  @After
  fun tearDown() {
    server.shutdown()

    BKTClient.destroy()
  }

  @Test
  fun `initialize - first call - success`() {
    server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(
          moshi.adapter(GetEvaluationsResponse::class.java)
            .toJson(
              GetEvaluationsResponse(
                GetEvaluationsDataResponse(
                  evaluations = user1Evaluations,
                  user_evaluations_id = "user_evaluations_id_value",
                ),
              ),
            ),
        ),
    )

    val future = BKTClient.initialize(
      ApplicationProvider.getApplicationContext(),
      config,
      user1.toBKTUser(),
      1000,
    )

    val result = future.get()

    // success
    assertThat(result).isNull()

    assertThat(server.requestCount).isEqualTo(1)

    val client = BKTClient.getInstance() as BKTClientImpl

    assertThat(client.component.evaluationInteractor.evaluations)
      .isEqualTo(mapOf(user1.id to user1Evaluations.evaluations))

    assertThat(client.component.dataModule.evaluationDao.get(user1.id))
      .isEqualTo(user1Evaluations.evaluations)

    Thread.sleep(100)

    val memoryEvents = client.component.eventInteractor.events.get()
    assertThat(memoryEvents).hasSize(2)
    assertGetEvaluationLatencyMetricsEvent(memoryEvents[0], mapOf("tag" to config.featureTag))
    assertGetEvaluationSizeMetricsEvent(
      memoryEvents[1],
      MetricsEventData.GetEvaluationSizeMetricsEvent(mapOf("tag" to config.featureTag), 734),
    )

    val dbEvents = client.component.dataModule.eventDao.getEvents()
    assertThat(dbEvents).hasSize(2)
    assertGetEvaluationLatencyMetricsEvent(dbEvents[0], mapOf("tag" to config.featureTag))
    assertGetEvaluationSizeMetricsEvent(
      dbEvents[1],
      MetricsEventData.GetEvaluationSizeMetricsEvent(mapOf("tag" to config.featureTag), 734),
    )
  }

  @Test
  fun `initialize - first call - timeout`() {
    server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBodyDelay(2, TimeUnit.SECONDS)
        .setBody(
          moshi.adapter(GetEvaluationsResponse::class.java)
            .toJson(
              GetEvaluationsResponse(
                GetEvaluationsDataResponse(
                  evaluations = user1Evaluations,
                  user_evaluations_id = "user_evaluations_id_value",
                ),
              ),
            ),
        ),
    )

    val future = BKTClient.initialize(
      ApplicationProvider.getApplicationContext(),
      config,
      user1.toBKTUser(),
      1000,
    )

    val result = future.get()

    // failure
    assertThat(result).isInstanceOf(BKTException.TimeoutException::class.java)

    assertThat(server.requestCount).isEqualTo(1)

    val client = BKTClient.getInstance() as BKTClientImpl

    assertThat(client.component.evaluationInteractor.evaluations)
      .isEqualTo(mapOf(user1.id to emptyList<Evaluation>()))
    assertThat(client.component.dataModule.evaluationDao.get(user1.id)).isEmpty()

    Thread.sleep(100)

    // timeout event should be saved
    val memoryEvents = client.component.eventInteractor.events.get()
    assertTimeoutErrorCountMetricsEvent(
      memoryEvents[0],
      MetricsEventData.TimeoutErrorCountMetricsEvent(config.featureTag),
    )

    val dbEvents = client.component.dataModule.eventDao.getEvents()
    assertThat(dbEvents).hasSize(1)
    assertTimeoutErrorCountMetricsEvent(
      dbEvents[0],
      MetricsEventData.TimeoutErrorCountMetricsEvent(config.featureTag),
    )
  }

  @Test
  fun `initialize - second call`() {
    server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBodyDelay(500, TimeUnit.MILLISECONDS)
        .setBody(
          moshi.adapter(GetEvaluationsResponse::class.java)
            .toJson(
              GetEvaluationsResponse(
                GetEvaluationsDataResponse(
                  evaluations = user1Evaluations,
                  user_evaluations_id = "user_evaluations_id_value",
                ),
              ),
            ),
        ),
    )
    server.enqueue(MockResponse().setResponseCode(500).setBody("500 error"))

    val future1 = BKTClient.initialize(
      ApplicationProvider.getApplicationContext(),
      config,
      user1.toBKTUser(),
      1000,
    )

    val future2 = BKTClient.initialize(
      ApplicationProvider.getApplicationContext(),
      config,
      user1.toBKTUser(),
      1000,
    )

    // future1 has not finished yet
    assertThat(future1.isDone).isFalse()

    // second call should finish immediately
    assertThat(future2.isDone).isTrue()
    assertThat(future2.get()).isNull()

    assertThat(future1.get()).isNull()
  }

  @Test
  fun destroy() {
    server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(
          moshi.adapter(GetEvaluationsResponse::class.java)
            .toJson(
              GetEvaluationsResponse(
                GetEvaluationsDataResponse(
                  evaluations = user1Evaluations,
                  user_evaluations_id = "user_evaluations_id_value",
                ),
              ),
            ),
        ),
    )

    BKTClient.initialize(
      ApplicationProvider.getApplicationContext(),
      config,
      user1.toBKTUser(),
      1000,
    )

    assertThat(BKTClient.getInstance()).isInstanceOf(BKTClient::class.java)

    BKTClient.destroy()

    assertThrows(BKTException.IllegalArgumentException::class.java) {
      BKTClient.getInstance()
    }
  }
}

// these assertion methods do not check full-equality, but that should be covered in other tests
fun assertGetEvaluationLatencyMetricsEvent(actual: Event, expectedLabels: Map<String, String>) {
  assertThat(actual.id).isNotEmpty() // id is not assertable here
  assertThat(actual.type).isEqualTo(EventType.METRICS)
  assertThat(actual.event).isInstanceOf(EventData.MetricsEvent::class.java)

  val actualMetricsEvent = actual.event as EventData.MetricsEvent

  assertThat(actualMetricsEvent.timestamp).isGreaterThan(0)
  assertThat(actualMetricsEvent.type).isEqualTo(MetricsEventType.GET_EVALUATION_LATENCY)
  assertThat(actualMetricsEvent.event)
    .isInstanceOf(MetricsEventData.GetEvaluationLatencyMetricsEvent::class.java)

  val actualLatencyEvent =
    actualMetricsEvent.event as MetricsEventData.GetEvaluationLatencyMetricsEvent

  assertThat(actualLatencyEvent.labels).isEqualTo(expectedLabels)
  // actualLatencyEvent.duration is not assertable
}

fun assertGetEvaluationSizeMetricsEvent(
  actual: Event,
  expectedSizeEvent: MetricsEventData.GetEvaluationSizeMetricsEvent,
) {
  assertThat(actual.id).isNotEmpty()
  assertThat(actual.type).isEqualTo(EventType.METRICS)
  assertThat(actual.event).isInstanceOf(EventData.MetricsEvent::class.java)

  val actualMetricsEvent = actual.event as EventData.MetricsEvent

  assertThat(actualMetricsEvent.timestamp).isGreaterThan(0)
  assertThat(actualMetricsEvent.type).isEqualTo(MetricsEventType.GET_EVALUATION_SIZE)
  assertThat(actualMetricsEvent.event)
    .isInstanceOf(MetricsEventData.GetEvaluationSizeMetricsEvent::class.java)

  val actualSizeEvent = actualMetricsEvent.event as MetricsEventData.GetEvaluationSizeMetricsEvent

  assertThat(actualSizeEvent).isEqualTo(expectedSizeEvent)
}

fun assertTimeoutErrorCountMetricsEvent(
  actual: Event,
  expectedMetricsEvent: MetricsEventData.TimeoutErrorCountMetricsEvent,
) {
  assertThat(actual.type).isEqualTo(EventType.METRICS)
  val actualMetricsEvent = actual.event as EventData.MetricsEvent
  assertThat(actualMetricsEvent.type).isEqualTo(MetricsEventType.TIMEOUT_ERROR_COUNT)
  assertThat(actualMetricsEvent.event).isEqualTo(expectedMetricsEvent)
}
