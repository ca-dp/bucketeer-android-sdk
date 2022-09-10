package jp.bucketeer.sdk

import android.content.Context
import android.util.Patterns
import jp.bucketeer.sdk.log.SdkInsideLogHandler
import jp.bucketeer.sdk.log.SdkLogger
import jp.bucketeer.sdk.log.UserLogHandler
import org.json.JSONObject
import java.util.regex.Matcher

interface Bucketeer {
  fun setUser(userId: String)
  fun setUser(userId: String, userData: Map<String, String>)
  fun getUser(): User?

  fun fetchUserEvaluations()
  fun fetchUserEvaluations(
    fetchUserEvaluationsCallback: FetchUserEvaluationsCallback? = null,
  )

  fun getEvaluation(featureId: String): Evaluation?

  fun getVariation(featureId: String, defaultValue: String): String
  fun getVariation(featureId: String, defaultValue: Int): Int
  fun getVariation(featureId: String, defaultValue: Long): Long
  fun getVariation(featureId: String, defaultValue: Float): Float
  fun getVariation(featureId: String, defaultValue: Double): Double
  fun getVariation(featureId: String, defaultValue: Boolean): Boolean
  fun getJsonVariation(featureId: String, defaultValue: JSONObject): JSONObject

  fun track(goalId: String)
  fun track(goalId: String, value: Double)

  fun start()
  fun stop()
  val isUserSet: Boolean

  interface FetchUserEvaluationsCallback {
    fun onSuccess()
    fun onError(exception: BucketeerException)
  }

  abstract class FetchUserEvaluationsCallbackAdapter : FetchUserEvaluationsCallback {
    override fun onSuccess() {}
    override fun onError(exception: BucketeerException) {}
  }

  class Builder(private val context: Context) {
    private var config: BucketeerConfig? = null
    private var apiKey: String? = null
    private var endpoint: String? = null
    private var featureTag: String? = null
    private var isLogcatLoggingEnable: Boolean = false

    fun config(config: BucketeerConfig): Builder {
      this.config = config
      return this
    }

    fun featureTag(featureTag: String): Builder {
      this.featureTag = featureTag
      return this
    }

    fun apiKey(apiKey: String): Builder {
      this.apiKey = apiKey
      return this
    }

    fun endpoint(endpoint: String): Builder {
      this.endpoint = endpoint
      return this
    }

    /**
     * Please use this method only for debugging
     * Please run following adb command for enabling DEBUG level log
     *
     * `adb shell setprop log.tag.Bucketeer DEBUG`
     */
    fun logcatLogging(enabled: Boolean): Builder {
      isLogcatLoggingEnable = enabled
      return this
    }

    @Throws(BucketeerException::class)
    fun build(): Bucketeer {
      if (BuildConfig.DEBUG) {
        SdkLogger.addLogger(SdkInsideLogHandler(TAG))
      } else if (isLogcatLoggingEnable) {
        SdkLogger.addLogger(UserLogHandler(TAG))
      }
      val apiKey = apiKey ?: throw BucketeerException.IllegalArgumentException(
        "api key is required. Please add builder.apiKey()",
      )
      val endpoint = endpoint ?: throw BucketeerException.IllegalArgumentException(
        "endpoint is required. Please add builder.endpoint()",
      )
      val m: Matcher = Patterns.WEB_URL.matcher(endpoint)
      if (!m.matches()) {
        throw BucketeerException.IllegalArgumentException(
          "endpoint is invalid",
        )
      }
      val featureTag = featureTag ?: throw BucketeerException.IllegalArgumentException(
        "feature tag is required. Please add builder.featureTag()",
      )
      return BucketeerImpl(
        context,
        apiKey,
        endpoint,
        featureTag,
        config ?: BucketeerConfig.DEFAULT,
      )
    }
  }

  companion object {
    private const val TAG = "Bucketeer"
  }
}
