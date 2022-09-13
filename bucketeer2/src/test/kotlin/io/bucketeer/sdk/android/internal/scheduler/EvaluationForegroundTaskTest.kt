package io.bucketeer.sdk.android.internal.scheduler

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import io.bucketeer.sdk.android.BKTConfig
import io.bucketeer.sdk.android.internal.di.ComponentImpl
import io.bucketeer.sdk.android.internal.di.DataModule
import io.bucketeer.sdk.android.internal.di.InteractorModule
import io.bucketeer.sdk.android.internal.model.response.GetEvaluationsDataResponse
import io.bucketeer.sdk.android.internal.model.response.GetEvaluationsResponse
import io.bucketeer.sdk.android.internal.remote.measureTimeMillisWithResult
import io.bucketeer.sdk.android.mocks.user1
import io.bucketeer.sdk.android.mocks.user1Evaluations
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class EvaluationForegroundTaskTest {
  private lateinit var server: MockWebServer

  private lateinit var component: ComponentImpl
  private lateinit var moshi: Moshi
  private lateinit var executor: ScheduledExecutorService
  private lateinit var task: EvaluationForegroundTask

  @Before
  fun setup() {
    server = MockWebServer()
    component = ComponentImpl(
      dataModule = DataModule(
        application = ApplicationProvider.getApplicationContext(),
        config = BKTConfig.builder()
          .endpoint(server.url("").toString())
          .apiKey("api_key_value")
          .featureTag("feature_tag_value")
          .eventsMaxQueueSize(3)
          .pollingInterval(1000)
          .build(),
        user = user1,
        inMemoryDB = true,
      ),
      interactorModule = InteractorModule(),
    )

    moshi = component.dataModule.moshi

    executor = Executors.newSingleThreadScheduledExecutor()

    task = EvaluationForegroundTask(component, executor)
  }

  @After
  fun tearDown() {
    task.stop()
    server.shutdown()
    executor.shutdownNow()
  }

  @Test
  fun start() {
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

    task.start()
    assertThat(server.requestCount).isEqualTo(0)

    val (time, _) = measureTimeMillisWithResult { server.takeRequest() }

    assertThat(server.requestCount).isEqualTo(1)
    assertThat(time).isGreaterThan(1000)
  }

  @Test
  fun `stop should cancel scheduling`() {
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

    task.start()
    task.stop()

    val request = server.takeRequest(2, TimeUnit.SECONDS)
    assertThat(request).isNull()
    assertThat(server.requestCount).isEqualTo(0)
  }
}
