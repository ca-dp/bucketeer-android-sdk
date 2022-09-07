package io.bucketeer.sdk.android.internal.evaluation

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import io.bucketeer.sdk.android.BKTConfig
import io.bucketeer.sdk.android.internal.di.Component
import io.bucketeer.sdk.android.internal.di.DataModule
import io.bucketeer.sdk.android.internal.di.InteractorModule
import io.bucketeer.sdk.android.internal.model.Evaluation
import io.bucketeer.sdk.android.internal.model.UserEvaluationsState
import io.bucketeer.sdk.android.internal.model.request.GetEvaluationsRequest
import io.bucketeer.sdk.android.internal.model.response.GetEvaluationsDataResponse
import io.bucketeer.sdk.android.internal.model.response.GetEvaluationsResponse
import io.bucketeer.sdk.android.internal.remote.GetEvaluationsResult
import io.bucketeer.sdk.android.mocks.evaluation1
import io.bucketeer.sdk.android.mocks.evaluation2
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

@RunWith(RobolectricTestRunner::class)
class EvaluationInteractorTest {
  private lateinit var server: MockWebServer

  private lateinit var component: Component
  private lateinit var moshi: Moshi

  private lateinit var interactor: EvaluationInteractor

  @Before
  fun setup() {
    server = MockWebServer()

    component = Component(
      dataModule = DataModule(
        application = ApplicationProvider.getApplicationContext(),
        config = BKTConfig.builder()
          .endpoint(server.url("").toString())
          .apiKey("api_key_value")
          .featureTag("feature_tag_value")
          .build(),
      ),
      interactorModule = InteractorModule(),
      executor = Executors.newSingleThreadScheduledExecutor()
    )

    interactor = component.evaluationInteractor

    moshi = component.dataModule.moshi
  }

  @After
  fun tearDown() {
    server.shutdown()
    component.executor.shutdownNow()
    component.dataModule.sharedPreferences.edit()
      .clear()
      .commit()
  }

  @Test
  fun `fetch - initial FULL`() {
    server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(
          moshi.adapter(GetEvaluationsResponse::class.java)
            .toJson(
              GetEvaluationsResponse(
                GetEvaluationsDataResponse(
                  state = UserEvaluationsState.FULL,
                  evaluations = user1Evaluations,
                  user_evaluations_id = "user_evaluations_id_value"
                )
              )
            )
        )
    )

    assertThat(interactor.currentEvaluationsId).isEmpty()

    val result = interactor.fetch(user1, null)

    // assert request
    assertThat(server.requestCount).isEqualTo(1)
    val request = server.takeRequest()
    val requestBody = moshi.adapter(GetEvaluationsRequest::class.java)
      .fromJson(request.body.readString(Charsets.UTF_8))

    assertThat(requestBody!!.user_evaluations_id).isEmpty()
    assertThat(requestBody.tag).isEqualTo(component.dataModule.config.featureTag)
    assertThat(requestBody.user).isEqualTo(user1)

    // assert response
    assertThat(result).isInstanceOf(GetEvaluationsResult.Success::class.java)

    assertThat(interactor.currentEvaluationsId).isEqualTo("user_evaluations_id_value")

    assertThat(interactor.latestEvaluations[user1.id]).isEqualTo(listOf(evaluation1, evaluation2))
    val latestEvaluations = component.dataModule.latestEvaluationDao.get(user1.id)
    assertThat(latestEvaluations).isEqualTo(listOf(evaluation1, evaluation2))

    // current evaluation should not be updated at this time
    assertThat(interactor.currentEvaluations[user1.id]).isEqualTo(emptyList<Evaluation>())
    val currentEvaluations = component.dataModule.currentEvaluationDao.getEvaluations(user1.id)
    assertThat(currentEvaluations).isEqualTo(emptyList<Evaluation>())
  }

  @Test
  fun `fetch - update FULL`() {
    // initial response(for preparation)
    server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(
          moshi.adapter(GetEvaluationsResponse::class.java)
            .toJson(
              GetEvaluationsResponse(
                GetEvaluationsDataResponse(
                  state = UserEvaluationsState.FULL,
                  evaluations = user1Evaluations,
                  user_evaluations_id = "user_evaluations_id_value"
                )
              )
            )
        )
    )
    interactor.fetch(user1, null)

    // update current evaluation
    interactor.getLatestAndRefreshCurrent(user1.id, evaluation1.feature_id)

    val newEvaluation = evaluation1.copy(
      variation_value = evaluation1.variation_value + "_updated"
    )
    // second response(test target)
    server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(
          moshi.adapter(GetEvaluationsResponse::class.java)
            .toJson(
              GetEvaluationsResponse(
                GetEvaluationsDataResponse(
                  state = UserEvaluationsState.FULL,
                  evaluations = user1Evaluations.copy(
                    evaluations = listOf(newEvaluation)
                  ),
                  user_evaluations_id = "user_evaluations_id_value_updated"
                )
              )
            )
        )
    )

    val result = interactor.fetch(user1, null)

    assertThat(server.requestCount).isEqualTo(2)

    assertThat(result).isInstanceOf(GetEvaluationsResult.Success::class.java)

    assertThat(interactor.currentEvaluationsId).isEqualTo("user_evaluations_id_value_updated")

    assertThat(interactor.latestEvaluations[user1.id]).isEqualTo(listOf(newEvaluation))
    val latestEvaluations = component.dataModule.latestEvaluationDao.get(user1.id)
    assertThat(latestEvaluations).isEqualTo(listOf(newEvaluation))

    assertThat(interactor.currentEvaluations[user1.id]).isEqualTo(listOf(evaluation1))
    val currentEvaluations = component.dataModule.currentEvaluationDao.getEvaluations(user1.id)
    assertThat(currentEvaluations).isEqualTo(listOf(evaluation1))
  }

  @Test
  fun `fetch - update PARTIAL`() {
    
  }
}
