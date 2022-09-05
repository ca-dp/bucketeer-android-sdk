package io.bucketeer.sdk.android.internal.remote

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import io.bucketeer.sdk.android.internal.di.DataModule
import io.bucketeer.sdk.android.internal.model.UserEvaluationsState
import io.bucketeer.sdk.android.internal.model.response.GetEvaluationsDataResponse
import io.bucketeer.sdk.android.internal.model.response.GetEvaluationsResponse
import io.bucketeer.sdk.android.mocks.user1
import io.bucketeer.sdk.android.mocks.user1Evaluations
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

internal class ApiClientImplTest {
  lateinit var server: MockWebServer
  lateinit var client: ApiClientImpl
  lateinit var endpoint: String
  lateinit var moshi: Moshi

  @Before
  fun setup() {
    server = MockWebServer()
    endpoint = server.url("").toString()
    moshi = DataModule.createMoshi()
  }

  @After
  fun tearDown() {
    server.shutdown()
  }

  @Test
  fun `fetchEvaluations - success`() {
    val expected = GetEvaluationsResponse(
      data = GetEvaluationsDataResponse(
        state = UserEvaluationsState.FULL,
        evaluations = user1Evaluations,
        user_evaluations_id = "user_evaluation_id"
      )
    )
    server.enqueue(
      MockResponse()
        .setBodyDelay(1, TimeUnit.SECONDS)
        .setBody(
          moshi.adapter(GetEvaluationsResponse::class.java).toJson(expected)
        )
        .setResponseCode(200)
    )

    client = ApiClientImpl(
      endpoint = endpoint,
      apiKey = "api_key_value",
      featureTag = "feature_tag_value",
      moshi = moshi
    )

    val result = client.fetchEvaluations(
      user = user1,
      userEvaluationsId = "user_evaluation_id"
    )

    assertThat(result).isInstanceOf(GetEvaluationsResult.Success::class.java)
    val success = result as GetEvaluationsResult.Success

    assertThat(success.value).isEqualTo(expected)

    assertThat(success.millis).isGreaterThan(TimeUnit.SECONDS.toMillis(1))
    assertThat(success.sizeByte).isEqualTo(737)
    assertThat(success.featureTag).isEqualTo("feature_tag_value")
    assertThat(success.state).isEqualTo(UserEvaluationsState.FULL)
  }
}
