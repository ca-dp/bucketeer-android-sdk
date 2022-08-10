package jp.bucketeer.sdk.test.e2e

import android.app.Application
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import bucketeer.event.client.EventOuterClass
import bucketeer.feature.EvaluationOuterClass
import bucketeer.feature.ReasonOuterClass
import bucketeer.user.UserOuterClass
import jp.bucketeer.sdk.Api
import jp.bucketeer.sdk.ApiClient
import jp.bucketeer.sdk.Bucketeer
import jp.bucketeer.sdk.BucketeerConfig
import jp.bucketeer.sdk.BucketeerException
import jp.bucketeer.sdk.Constants
import jp.bucketeer.sdk.database.DatabaseOpenHelper
import jp.bucketeer.sdk.evaluation.db.CurrentEvaluationEntity
import jp.bucketeer.sdk.evaluation.db.LatestEvaluationEntity
import jp.bucketeer.sdk.events.EventEntity
import jp.bucketeer.sdk.events.pack
import jp.bucketeer.sdk.test.BuildConfig
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeGreaterOrEqualTo
import org.amshove.kluent.shouldNotBe
import org.junit.Assert
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@LargeTest
class BucketeerTest {

  private val sql_tables = listOf(
    CurrentEvaluationEntity.TABLE_NAME,
    LatestEvaluationEntity.TABLE_NAME,
    EventEntity.TABLE_NAME
  )

  @Before
  fun beforeExecute() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
    val sqLiteOpenHelper = DatabaseOpenHelper(context as Application)
    for (table in sql_tables) {
      sqLiteOpenHelper.writableDatabase.delete(
        table,
        null,
        null
      )
    }
    context.getSharedPreferences(
      Constants.PREFERENCES_NAME,
      Context.MODE_PRIVATE
    ).edit().clear().commit()
  }

  @Test
  fun fetchEvaluationUserEvaluationsIdEmpty() {
    val api = createApi()
    when (val result = api.fetchEvaluation(user1, "")) {
      is Api.Result.Success -> {
        val response = result.value
        response.state shouldBeEqualTo EvaluationOuterClass.UserEvaluations.State.FULL
        response.userEvaluationsId shouldNotBe ""
        response.evaluations.evaluationsCount shouldBeGreaterOrEqualTo 1
      }
      is Api.Result.Fail -> {
        fail("Failed to fetch evaluation")
      }
    }
  }

  @Test
  fun fetchEvaluationUserEvaluationsId() {
    val api = createApi()
    var userEvaluationsId = ""
    when (val result = api.fetchEvaluation(user1, userEvaluationsId1)) {
      is Api.Result.Success -> {
        val response = result.value
        response.userEvaluationsId shouldNotBe ""
        userEvaluationsId = response.userEvaluationsId
        response.state shouldBeEqualTo EvaluationOuterClass.UserEvaluations.State.FULL
        response.evaluations.evaluationsCount shouldBeGreaterOrEqualTo 1
      }
      is Api.Result.Fail -> {
        fail("Failed to fetch evaluation")
      }
    }
    when (val result = api.fetchEvaluation(user1, userEvaluationsId)) {
      is Api.Result.Success -> {
        val response = result.value
        response.userEvaluationsId shouldNotBe ""
        response.state shouldBeEqualTo EvaluationOuterClass.UserEvaluations.State.FULL
        response.evaluations.evaluationsCount shouldBe 0
      }
      is Api.Result.Fail -> {
        fail("Failed to fetch evaluation")
      }
    }
  }

  @Test
  fun registerEvent() {
    val api = createApi()
    val event1 = EventOuterClass.Event
      .newBuilder()
      .setId(UUID.randomUUID().toString())
      .setEvent(evaluationEvent1.pack())
      .build()
    val event2 = EventOuterClass.Event
      .newBuilder()
      .setId(UUID.randomUUID().toString())
      .setEvent(goalEvent1.pack())
      .build()
    val events = listOf(event1, event2)
    when (val result = api.registerEvent(events)) {
      is Api.Result.Success -> {
        val response = result.value
        response.errorsCount shouldBeEqualTo 0
      }
      is Api.Result.Fail -> {
        fail("Failed to register event")
      }
    }
  }

  @Test
  fun getVariation() {
    val bucketeer = createBucketeer()
    bucketeer.setUser(USER_ID_1)
    val callbackCountDown = CountDownLatch(1)
    bucketeer.fetchUserEvaluations(object : Bucketeer.FetchUserEvaluationsCallback {
      override fun onSuccess() {
        callbackCountDown.countDown()
      }

      override fun onError(exception: BucketeerException) {
        fail(exception.message)
      }
    })
    Assert.assertTrue(callbackCountDown.await(10, TimeUnit.SECONDS))
    bucketeer.getVariation(FEATURE_FLAG_ID_1, "default") shouldBeEqualTo FEATURE_FLAG_1_VARIATION
  }

  @Test
  fun getEvaluation() {
    val bucketeer = createBucketeer()
    bucketeer.setUser(USER_ID_1)
    val callbackCountDown = CountDownLatch(1)
    bucketeer.fetchUserEvaluations(object : Bucketeer.FetchUserEvaluationsCallback {
      override fun onSuccess() {
        callbackCountDown.countDown()
      }

      override fun onError(exception: BucketeerException) {
        fail(exception.message)
      }
    })
    Assert.assertTrue(callbackCountDown.await(10, TimeUnit.SECONDS))
    bucketeer.getEvaluation(FEATURE_FLAG_ID_1)!!.run {
      featureId shouldBeEqualTo FEATURE_FLAG_ID_1
      variationValue shouldBeEqualTo FEATURE_FLAG_1_VARIATION
      userId shouldBeEqualTo USER_ID_1
    }
  }

  private fun createApi(): Api {
    return ApiClient(BuildConfig.API_KEY, BuildConfig.API_URL, TAG)
  }

  private fun createBucketeer(): Bucketeer {
    val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
    val config = BucketeerConfig.Builder().logSendingIntervalMillis(100)
      .logSendingMaxBatchQueueCount(1)
      .pollingEvaluationIntervalMillis(100)
      .build()
    return Bucketeer.Builder(context).config(config)
      .apiKey(BuildConfig.API_KEY)
      .endpoint(BuildConfig.API_URL)
      .featureTag(TAG)
      .build()
  }

  private val userEvaluationsId1: String by lazy {
    "user-evaluations-id-1"
  }

  private val timestamp = System.currentTimeMillis() / 1000

  private val user1 = UserOuterClass
    .User
    .newBuilder()
    .setId(USER_ID_1)
    .build()

  private val reason = ReasonOuterClass.Reason.newBuilder()
    .setType(ReasonOuterClass.Reason.Type.CLIENT)
    .build()

  private val evaluationEvent1 = EventOuterClass.EvaluationEvent
    .newBuilder()
    .setTimestamp(timestamp)
    .setFeatureId(FEATURE_FLAG_ID_1)
    .setFeatureVersion(0)
    .setUserId(USER_ID_1)
    .setVariationId(userEvaluationsId1)
    .setUser(user1)
    .setReason(reason)
    .setTag(TAG)
    .setSourceId(EventOuterClass.SourceId.ANDROID)
    .build()

  private val evaluation1 = EvaluationOuterClass.Evaluation
    .newBuilder()
    .setFeatureId(FEATURE_FLAG_ID_1)
    .setFeatureVersion(0)
    .setUserId(USER_ID_1)
    .setVariationId(userEvaluationsId1)
    .setReason(reason)
    .build()

  private val goalEvent1 = EventOuterClass.GoalEvent
    .newBuilder()
    .setTimestamp(timestamp)
    .setGoalId(GOAL_ID_1)
    .setUserId(USER_ID_1)
    .setValue(GOAL_VALUE_1)
    .setUser(user1)
    .addEvaluations(evaluation1)
    .setTag(TAG)
    .setSourceId(EventOuterClass.SourceId.ANDROID)
    .build()

  companion object {
    const val TAG = "android"
    const val USER_ID_1 = "bucketeer-android-user-id-1"
    const val FEATURE_FLAG_ID_1 = "feature-android-e2e-1"
    const val FEATURE_FLAG_1_VARIATION = "value-1"
    const val GOAL_ID_1 = "goal-android-e2e-1"
    const val GOAL_VALUE_1 = 1.0
  }
}
