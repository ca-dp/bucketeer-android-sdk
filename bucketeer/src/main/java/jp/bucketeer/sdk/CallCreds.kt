package jp.bucketeer.sdk

import io.grpc.CallCredentials
import io.grpc.Metadata
import io.grpc.Status
import java.util.concurrent.Executor

internal class CallCreds(private val token: String) : CallCredentials() {

  override fun applyRequestMetadata(
    request: RequestInfo,
    executor: Executor,
    applier: MetadataApplier,
  ) {
    executor.execute {
      val headers = Metadata()
      val key = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER)
      headers.put(key, token)
      try {
        applier.apply(headers)
      } catch (e: Throwable) {
        applier.fail(Status.UNAUTHENTICATED.withCause(e))
      }
    }
  }

  override fun thisUsesUnstableApi() {}
}
