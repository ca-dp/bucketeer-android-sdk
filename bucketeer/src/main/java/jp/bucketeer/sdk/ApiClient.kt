package jp.bucketeer.sdk

import androidx.annotation.VisibleForTesting
import bucketeer.event.client.EventOuterClass
import bucketeer.gateway.GatewayGrpc
import bucketeer.gateway.Service
import bucketeer.user.UserOuterClass
import io.grpc.StatusRuntimeException
import io.grpc.okhttp.OkHttpChannelBuilder
import jp.bucketeer.sdk.log.logd
import jp.bucketeer.sdk.log.loge

internal class ApiClient(
    apiKey: String,
    endpoint: String,
    private val featureTag: String
) : Api {
  var fetchEvaluationsApiCallback: FetchEvaluationsApiCallback? = null
  private val client: GatewayGrpc.GatewayBlockingStub

  init {
    val channel = OkHttpChannelBuilder.forAddress(endpoint, getEndpointPort(endpoint))
        .useTransportSecurity()
        .build()
    // This workaround helps to improve the latency of the first request
    try {
      channel.getState(true)
    } catch (e: UnsupportedOperationException) {
      loge(throwable = e) { "ApiClient getState failed" }
    }
    val creds = CallCreds(apiKey)
    client = GatewayGrpc.newBlockingStub(channel).withCallCredentials(creds)
  }

  override fun setFetchEvaluationApiCallback(f: FetchEvaluationsApiCallback) {
    fetchEvaluationsApiCallback = f
  }

  override fun fetchEvaluation(
      user: UserOuterClass.User, userEvaluationsId: String) : Api.Result<Service.GetEvaluationsResponse> {
    return try {
      val request = Service.GetEvaluationsRequest.newBuilder()
          .setUser(user)
          .setUserEvaluationsId(userEvaluationsId)
          .setTag(featureTag).build()
      logd { "--> Fetch Evaluation\n$request" }
      val startTime = System.currentTimeMillis()
      val response = client.getEvaluations(request)
      logd { "--> END Fetch Evaluation" }
      logd { "<-- Fetch Evaluation\n$response\n<-- END Evaluation response" }
      val endTime = System.currentTimeMillis()
      fetchEvaluationsApiCallback?.onSuccess(endTime - startTime, response.getSerializedSize(),
          featureTag, response.state.name)
      Api.Result.Success(response)
    } catch (e: StatusRuntimeException) {
      logd(throwable = e) { "<-- Fetch Evaluation error" }
      fetchEvaluationsApiCallback?.onFailure(featureTag, e)
      Api.Result.Fail(e.toBucketeerException())
    }
  }

  override fun registerEvent(
      events: List<EventOuterClass.Event>
  ): Api.Result<Service.RegisterEventsResponse> {
    return try {
      val registerRequestBuilder = Service.RegisterEventsRequest.newBuilder()
      val request = registerRequestBuilder
          .addAllEvents(events)
          .build()
      logd { "--> Register events\n$request" }
      val response = client.registerEvents(request)
      logd { "--> END Register events" }
      logd { "<-- Register events\n$response\n<-- END Register events" }
      Api.Result.Success(response)
    } catch (e: StatusRuntimeException) {
      logd(throwable = e) { "<-- Register events error" }
      Api.Result.Fail(e.toBucketeerException())
    }
  }

  @VisibleForTesting
  fun getEndpointPort(endpoint: String) : Int {
    // FIXME: This condition should be removed once the L7 migration is done
    if (endpoint.contains(ENDPOINT_L7_PREFIX)) {
      return ENDPOINT_L7_PORT
    }
    return ENDPOINT_L4_PORT
  }

  companion object {
    const val ENDPOINT_L4_PORT = 9000
    const val ENDPOINT_L7_PORT = 443
    const val ENDPOINT_L7_PREFIX = "api-"
  }
}
