package jp.bucketeer.sdk.test.integration

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import bucketeer.event.client.EventOuterClass
import bucketeer.feature.EvaluationOuterClass
import bucketeer.gateway.Service
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.atLeastOnce
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import jp.bucketeer.sdk.Api
import jp.bucketeer.sdk.Bucketeer
import jp.bucketeer.sdk.BucketeerConfig
import jp.bucketeer.sdk.BucketeerImpl
import jp.bucketeer.sdk.Constants
import jp.bucketeer.sdk.database.DatabaseOpenHelper
import jp.bucketeer.sdk.di.DataModule
import jp.bucketeer.sdk.evaluation
import jp.bucketeer.sdk.evaluation.db.CurrentEvaluationEntity
import jp.bucketeer.sdk.evaluation.db.LatestEvaluationEntity
import jp.bucketeer.sdk.evaluation1
import jp.bucketeer.sdk.evaluation3
import jp.bucketeer.sdk.events.EventEntity
import jp.bucketeer.sdk.user
import jp.bucketeer.sdk.user1
import jp.bucketeer.sdk.user2
import jp.bucketeer.sdk.userEvaluationsId1
import jp.bucketeer.sdk.userEvaluationsId2
import jp.bucketeer.sdk.util.userOf
import jp.bucketeer.sdk.variation1
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Assert
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@MediumTest
class BucketeerTest {

  private val sql_tables = listOf(
      CurrentEvaluationEntity.TABLE_NAME,
      LatestEvaluationEntity.TABLE_NAME,
      EventEntity.TABLE_NAME
  )

  @Before
  fun beforeExecute() {
    val context = getInstrumentation().targetContext.applicationContext
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
  fun getUser() {
    val bucketeer = createBucketeer()
    bucketeer.setUser(
        user.id,
        user.data
    )
    bucketeer.getUser()?.run {
      id shouldBeEqualTo user.id
      data["gender"] shouldBeEqualTo user.data["gender"]
      data["age"] shouldBeEqualTo user.data["age"]
    } ?: fail("User is null")
  }

  @Test
  fun getUserIsNull() {
    val bucketeer = createBucketeer()
    bucketeer.getUser() shouldBe null
  }

  @Test
  fun getEvaluation() {
    val bucketeer = initBucketeer(userId = user1.id)
    val callbackCountDown = CountDownLatch(1)
    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed({
      bucketeer.getEvaluation(evaluation.featureId)?.run {
        id shouldBeEqualTo evaluation.id
        featureId shouldBeEqualTo evaluation.featureId
        featureVersion shouldBeEqualTo evaluation.featureVersion
        userId shouldBeEqualTo evaluation.userId
        variationId shouldBeEqualTo evaluation.variationId
        variationValue shouldBeEqualTo evaluation.variationValue
        reason shouldBeEqualTo evaluation.reason
      } ?: fail("Evaluation is null.")
      callbackCountDown.countDown()
    }, 300)
    Assert.assertTrue(callbackCountDown.await(5, TimeUnit.SECONDS))
  }

  @Test
  fun getEvaluationIsNull() {
    val bucketeer = createBucketeer()
    bucketeer.getEvaluation("feature-id") shouldBe null
  }

  @Test
  fun getVariation_default() {
    val bucketeer = initBucketeer(userId = user1.id)
    val callbackCountDown = CountDownLatch(1)
    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed({
      bucketeer.getVariation(
          "test",
          "default"
      ) shouldBeEqualTo "default"
      callbackCountDown.countDown()
    }, 300)
    Assert.assertTrue(callbackCountDown.await(5, TimeUnit.SECONDS))
  }

  @Test
  fun getVariation_getValue() {
    val bucketeer = initBucketeer(userId = user1.id)
    val callbackCountDown = CountDownLatch(1)
    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed({
      bucketeer.getVariation(
          evaluation1.featureId,
          "default"
      ) shouldBeEqualTo variation1.value
      callbackCountDown.countDown()
    }, 300)
    Assert.assertTrue(callbackCountDown.await(5, TimeUnit.SECONDS))
  }

  @Test
  fun getVariation_sendEvaluation() {
    val api: Api = mock()
    whenever(api.registerEvent(any()))
        .doReturn(Api.Result.Success(Service.RegisterEventsResponse.getDefaultInstance()))
    val bucketeer = initBucketeer(
        userId = user1.id,
        api = api
    )
    val callbackCountDown = CountDownLatch(1)
    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed({
      bucketeer.getVariation(
          evaluation1.featureId,
          "default"
      ) shouldBeEqualTo evaluation1.variation.value

      handler.postDelayed({
        argumentCaptor<List<EventOuterClass.Event>>().apply {
          verify(api, times(1)).registerEvent(capture())
          allValues.size shouldBe 1
          EventOuterClass
              .EvaluationEvent
              .parseFrom(firstValue[0].event.value)
              .variationId shouldBeEqualTo evaluation1.variationId
          callbackCountDown.countDown()
        }
      }, 300)
    }, 300)
    Assert.assertTrue(callbackCountDown.await(5, TimeUnit.SECONDS))
  }

  @Test
  fun track_sendGoal() {
    val api: Api = mock()
    whenever(api.registerEvent(any()))
        .doReturn(Api.Result.Success(Service.RegisterEventsResponse.getDefaultInstance()))
    val bucketeer = initBucketeer(
        userId = user1.id,
        api = api
    )

    val callbackCountDown = CountDownLatch(1)
    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed({
      bucketeer.getVariation(
          evaluation1.featureId,
          "default"
      ) shouldBeEqualTo evaluation1.variation.value

      val value = 1.0
      val goalId = "goal"
      bucketeer.track(
          goalId,
          value
      )
      handler.postDelayed({
        argumentCaptor<List<EventOuterClass.Event>>().apply {
          verify(api, times(2)).registerEvent(capture())
          allValues.size shouldBe 2
          val evaluationEvent = EventOuterClass
              .EvaluationEvent
              .parseFrom(firstValue[0].event.value)
          val goalEvent = EventOuterClass
              .GoalEvent
              .parseFrom(secondValue[0].event.value)
          evaluationEvent.variationId shouldBeEqualTo evaluation1.variationId
          goalEvent.goalId shouldBeEqualTo goalId
          goalEvent.value shouldBeEqualTo value
          goalEvent.evaluationsList[0].variationId shouldBeEqualTo evaluation1.variationId
          callbackCountDown.countDown()
        }
      }, 300)
    }, 300)
    Assert.assertTrue(callbackCountDown.await(5, TimeUnit.SECONDS))
  }

  @Test
  fun switchUser() {
    val api: Api = mock()
    whenever(api.registerEvent(any()))
        .doReturn(Api.Result.Success(Service.RegisterEventsResponse.getDefaultInstance()))
    val response2 = createEvaluationsResponse(evaluation3, userEvaluationsId2)
    val bucketeer = initBucketeer(
        userId = user1.id,
        api = api
    )

    val callbackCountDown = CountDownLatch(1)
    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed({
      bucketeer.getVariation(
          evaluation1.featureId,
          "default"
      ) shouldBeEqualTo evaluation1.variation.value

      val user = userOf(user2.id, mapOf())
      whenever(api.fetchEvaluation(any(), any())).doReturn(Api.Result.Success(response2))
      bucketeer.setUser(user.id, user.dataMap)
      handler.postDelayed({
        verify(api, atLeastOnce()).fetchEvaluation(user, userEvaluationsId1)
        bucketeer.getVariation(
            evaluation3.featureId,
            "default"
        ) shouldBeEqualTo evaluation3.variation.value

        handler.postDelayed({
          argumentCaptor<List<EventOuterClass.Event>>().apply {
            verify(api, times(2)).registerEvent(capture())
            println(allValues)
            allValues.size shouldBe 2
            val evaluationEvent = EventOuterClass
                .EvaluationEvent
                .parseFrom(allValues[0][0].event.value)
            val evaluationEvent2 = EventOuterClass
                .EvaluationEvent
                .parseFrom(allValues[1][0].event.value)

            evaluationEvent.variationId shouldBeEqualTo evaluation1.variationId
            evaluationEvent.userId shouldBeEqualTo user1.id

            evaluationEvent2.variationId shouldBeEqualTo evaluation3.variationId
            evaluationEvent2.userId shouldBeEqualTo user2.id
          }
          callbackCountDown.countDown()
        }, 300)
      }, 300)
    }, 300)
    Assert.assertTrue(callbackCountDown.await(5, TimeUnit.SECONDS))
  }

  private fun createEvaluationsResponse(
      evaluation: EvaluationOuterClass.Evaluation = evaluation1,
      userEvaluationsId: String = userEvaluationsId1
  ): Service.GetEvaluationsResponse {
    return Service.GetEvaluationsResponse.newBuilder()
        .setEvaluations(
            EvaluationOuterClass.UserEvaluations.newBuilder()
                .addAllEvaluations(listOf(evaluation))
        )
        .setState(EvaluationOuterClass.UserEvaluations.State.FULL)
        .setUserEvaluationsId(userEvaluationsId)
        .build()
  }

  private fun initBucketeer(
      response: Service.GetEvaluationsResponse = createEvaluationsResponse(),
      userId: String,
      api: Api = mock()
  ): Bucketeer {
    whenever(api.fetchEvaluation(any(), any())).doReturn(Api.Result.Success(response))
    val context = getInstrumentation().targetContext.applicationContext
    val config = BucketeerConfig.Builder().logSendingIntervalMillis(100)
        .logSendingMaxBatchQueueCount(1)
        .pollingEvaluationIntervalMillis(100)
        .build()
    val dataModule = spy(DataModule(
        context as Application,
        "",
        "api.local.bucketeer.jp",
        "android"))
    whenever(dataModule.api).doReturn(api)

    val bucketeer = BucketeerImpl(
        context = context,
        apiKey = "",
        endpoint = "",
        featureTag = "android",
        config = config,
        dataModule = dataModule
    )
    bucketeer.setUser(userId)

    bucketeer.start()
    Espresso.onIdle()

    return bucketeer
  }

  private fun createBucketeer(): Bucketeer {
    val context = getInstrumentation().targetContext.applicationContext
    val config = BucketeerConfig.Builder().logSendingIntervalMillis(100)
        .logSendingMaxBatchQueueCount(1)
        .pollingEvaluationIntervalMillis(100)
        .build()
    return Bucketeer.Builder(context).config(config)
        .apiKey("api-key")
        .endpoint("api.local.bucketeer.jp")
        .featureTag("android")
        .build()
  }
}
