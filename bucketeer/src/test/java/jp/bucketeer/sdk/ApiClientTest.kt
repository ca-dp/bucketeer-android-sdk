package jp.bucketeer.sdk

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class ApiClientTest {

  @Test
  fun getEndpointPortL4SingleLevel() {
    val endpoint = "api.bucketeer.jp"
    val api = ApiClient("apiKey", endpoint, "featureTag")
    val port = api.getEndpointPort(endpoint)
    port shouldBeEqualTo ApiClient.ENDPOINT_L4_PORT
  }

  @Test
  fun getEndpointPortL4MultiLevel() {
    val endpoint = "api.dev.bucketeer.jp"
    val api = ApiClient("apiKey", endpoint, "featureTag")
    val port = api.getEndpointPort(endpoint)
    port shouldBeEqualTo ApiClient.ENDPOINT_L4_PORT
  }

  @Test
  fun getEndpointPortL7() {
    val endpoint = "api-dev.bucketeer.jp"
    val api = ApiClient("apiKey", endpoint, "featureTag")
    val port = api.getEndpointPort(endpoint)
    port shouldBeEqualTo ApiClient.ENDPOINT_L7_PORT
  }
}
