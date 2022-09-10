package jp.bucketeer.sdk.log

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test

class SdkLogHandlerTest {
  private val logHistory: MutableList<String> = mutableListOf()

  @Before
  fun setUp() {
    SdkLogger.addLogger(
      object : LogHandler() {
        override fun log(
          priority: Int,
          msgCreator: (() -> String?)?,
          th: Throwable?,
          isLogForUser: Boolean,
        ) {
          logHistory += "$priority ${msgCreator?.invoke()} ${th?.javaClass?.simpleName}"
        }
      },
    )
  }

  @Test
  fun log() {
    logv { "VERBOSE" }
    logv(throwable = Exception()) { "VERBOSE" }
    logd { "DEBUG" }
    logd(throwable = Exception()) { "DEBUG" }
    logi { "INFO" }
    logi(throwable = Exception()) { "INFO" }
    logw { "WARN" }
    logw(throwable = Exception()) { "WARN" }
    loge { "ERROR" }
    loge(throwable = Exception()) { "ERROR" }
    logwtf { "ASSERT" }
    logwtf(throwable = Exception()) { "ASSERT" }

    logHistory shouldBeEqualTo listOf(
      "2 VERBOSE null",
      "2 VERBOSE Exception",
      "3 DEBUG null",
      "3 DEBUG Exception",
      "4 INFO null",
      "4 INFO Exception",
      "5 WARN null",
      "5 WARN Exception",
      "6 ERROR null",
      "6 ERROR Exception",
      "7 ASSERT null",
      "7 ASSERT Exception",
    )
  }
}
