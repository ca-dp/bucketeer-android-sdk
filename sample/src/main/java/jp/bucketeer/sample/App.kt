package jp.bucketeer.sample

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.facebook.stetho.Stetho
import jp.bucketeer.sdk.Bucketeer
import jp.bucketeer.sdk.Bucketeer.FetchUserEvaluationsCallbackAdapter
import jp.bucketeer.sdk.BucketeerConfig
import jp.bucketeer.sdk.BucketeerException

class App : Application(), LifecycleObserver {
  var bucketeer: Bucketeer? = null
    private set

  private val sharedPref by lazy {
    getSharedPreferences(
      Constants.PREFERENCE_FILE_KEY,
      Context.MODE_PRIVATE,
    )
  }

  override fun onCreate() {
    super.onCreate()
    Stetho.initializeWithDefaults(this)
    initBucketeer()
  }

  private fun initBucketeer() {
    val config = BucketeerConfig.Builder().logSendingIntervalMillis(
      20000,
    )
      .logSendingMaxBatchQueueCount(10)
      .pollingEvaluationIntervalMillis(20000)
      .build()
    try {
      bucketeer = Bucketeer.Builder(this).config(config)
        .apiKey(BuildConfig.API_KEY)
        .endpoint(BuildConfig.API_URL)
        .featureTag(getTag())
        .logcatLogging(true)
        .build()
    } catch (e: BucketeerException) {
      Log.e(TAG, e.message, e)
    }
    bucketeer?.run {
      fetchEvaluations(this)
      ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver(this))
    }
  }

  private fun fetchEvaluations(bucketeer: Bucketeer) {
    bucketeer.setUser(getUserId())
    bucketeer.fetchUserEvaluations(
      object : FetchUserEvaluationsCallbackAdapter() {
        override fun onSuccess() {
          Toast.makeText(this@App, "User Evaluations has been updated", Toast.LENGTH_LONG).show()
        }

        override fun onError(exception: BucketeerException) {
          Toast.makeText(this@App, "onError: $exception", Toast.LENGTH_LONG).show()
          exception.printStackTrace()
        }
      },
    )
  }

  private fun getTag(): String {
    return sharedPref.getString(
      Constants.PREFERENCE_KEY_TAG,
      Constants.DEFAULT_TAG,
    ) ?: Constants.DEFAULT_TAG
  }

  private fun getUserId(): String {
    return sharedPref.getString(
      Constants.PREFERENCE_KEY_USER_ID,
      Constants.DEFAULT_USER_ID,
    ) ?: Constants.DEFAULT_USER_ID
  }

  companion object {
    internal const val TAG = "BucketeerSample"
  }
}

internal class AppLifecycleObserver(
  private val bucketeer: Bucketeer,
) : DefaultLifecycleObserver {

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    bucketeer.start()
  }

  override fun onPause(owner: LifecycleOwner) {
    super.onPause(owner)
    bucketeer.stop()
  }
}
