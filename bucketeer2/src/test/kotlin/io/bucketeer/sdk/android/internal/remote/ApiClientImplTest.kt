package io.bucketeer.sdk.android.internal.remote

import com.google.common.truth.Truth.assertThat
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.squareup.moshi.Moshi
import io.bucketeer.sdk.android.BKTException
import io.bucketeer.sdk.android.internal.di.DataModule
import io.bucketeer.sdk.android.internal.model.UserEvaluationsState
import io.bucketeer.sdk.android.internal.model.response.ErrorResponse
import io.bucketeer.sdk.android.internal.model.response.GetEvaluationsDataResponse
import io.bucketeer.sdk.android.internal.model.response.GetEvaluationsResponse
import io.bucketeer.sdk.android.mocks.user1
import io.bucketeer.sdk.android.mocks.user1Evaluations
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(TestParameterInjector::class)
internal class ApiClientImplTest {
  private lateinit var server: MockWebServer
  private lateinit var client: ApiClientImpl
  private lateinit var endpoint: String
  private lateinit var moshi: Moshi

  @Suppress("unused")
  enum class ErrorTestCase(
    val code: Int,
    val expected: Class<*>
  ) {
    BAD_REQUEST(400, BKTException.BadRequestException::class.java),
    UNAUTHORIZED(401, BKTException.UnauthorizedException::class.java),
    NOT_FOUND(404, BKTException.FeatureNotFoundException::class.java),
    METHOD_NOT_ALLOWED(405, BKTException.InvalidHttpMethodException::class.java),
    INTERNAL_SERVER_ERROR(500, BKTException.ApiServerException::class.java)
  }

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
  fun `getEvaluations - success`() {
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

    val result = client.getEvaluations(
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

  @Test
  fun `getEvaluations - default timeout`() {
    client = ApiClientImpl(
      endpoint = endpoint,
      apiKey = "api_key_value",
      featureTag = "feature_tag_value",
      moshi = moshi,
      defaultRequestTimeoutMillis = 1_000
    )

    val (millis, result) = measureTimeMillisWithResult {
      client.getEvaluations(
        user = user1,
        userEvaluationsId = "user_evaluation_id"
      )
    }

    assertThat(millis).isGreaterThan(1_000)
    assertThat(millis).isLessThan(1_500)

    assertThat(result).isInstanceOf(GetEvaluationsResult.Failure::class.java)
    val failure = result as GetEvaluationsResult.Failure

    println(failure.error.cause)

    assertThat(failure.error).isInstanceOf(BKTException.TimeoutException::class.java)
    assertThat(failure.featureTag).isEqualTo("feature_tag_value")
  }

  @Test
  fun `getEvaluations - custom timeout`() {
    client = ApiClientImpl(
      endpoint = endpoint,
      apiKey = "api_key_value",
      featureTag = "feature_tag_value",
      moshi = moshi
    )

    val (millis, result) = measureTimeMillisWithResult {
      client.getEvaluations(
        user = user1,
        userEvaluationsId = "user_evaluation_id",
        timeoutMillis = TimeUnit.SECONDS.toMillis(1)
      )
    }

    assertThat(millis).isGreaterThan(1_000)
    assertThat(millis).isLessThan(1_500)

    assertThat(result).isInstanceOf(GetEvaluationsResult.Failure::class.java)
    val failure = result as GetEvaluationsResult.Failure

    println(failure.error.cause)

    assertThat(failure.error).isInstanceOf(BKTException.TimeoutException::class.java)
    assertThat(failure.featureTag).isEqualTo("feature_tag_value")
  }

  @Test
  fun `getEvaluations - network error`() {
    client = ApiClientImpl(
      endpoint = "https://thisdoesnotexist.bucketeer.io",
      apiKey = "api_key_value",
      featureTag = "feature_tag_value",
      moshi = moshi
    )

    val result = client.getEvaluations(
      user = user1,
      userEvaluationsId = "user_evaluation_id"
    )

    assertThat(result).isInstanceOf(GetEvaluationsResult.Failure::class.java)
    val failure = result as GetEvaluationsResult.Failure

    println(failure.error.cause)
  }


  @Test
  fun `getEvaluations - error with body`(@TestParameter case: ErrorTestCase) {
    server.enqueue(
      MockResponse()
        .setResponseCode(case.code)
        .setBody(
          moshi.adapter(ErrorResponse::class.java)
            .toJson(
              ErrorResponse(
                ErrorResponse.ErrorDetail(
                  code = case.code,
                  message = "error: ${case.code}"
                )
              )
            )
        )
    )
    client = ApiClientImpl(
      endpoint = endpoint,
      apiKey = "api_key_value",
      featureTag = "feature_tag_value",
      moshi = moshi
    )

    val result = client.getEvaluations(
      user = user1,
      userEvaluationsId = "user_evaluation_id"
    )

    assertThat(result).isInstanceOf(GetEvaluationsResult.Failure::class.java)
    val failure = result as GetEvaluationsResult.Failure
    val error = failure.error

    assertThat(error).isInstanceOf(case.expected)
    assertThat(error.message).isEqualTo("error: ${case.code}")
  }

  @Test
  fun `getEvaluations - error without body`(@TestParameter case: ErrorTestCase) {
    server.enqueue(
      MockResponse()
        .setResponseCode(case.code)
        .setBody("error: ${case.code}")
    )
    client = ApiClientImpl(
      endpoint = endpoint,
      apiKey = "api_key_value",
      featureTag = "feature_tag_value",
      moshi = moshi
    )

    val result = client.getEvaluations(
      user = user1,
      userEvaluationsId = "user_evaluation_id"
    )

    assertThat(result).isInstanceOf(GetEvaluationsResult.Failure::class.java)
    val failure = result as GetEvaluationsResult.Failure
    val error = failure.error

    assertThat(error).isInstanceOf(case.expected)
    assertThat(error.message).doesNotContain("${case.code}")
  }
}
