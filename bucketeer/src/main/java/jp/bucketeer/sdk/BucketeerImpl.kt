package jp.bucketeer.sdk

import android.app.Application
import android.content.Context
import com.google.android.gms.security.ProviderInstaller
import io.grpc.Status
import io.grpc.StatusRuntimeException
import jp.bucketeer.sdk.di.ActionCreatorModule
import jp.bucketeer.sdk.di.DataModule
import jp.bucketeer.sdk.di.StoreModule
import jp.bucketeer.sdk.dispatcher.Dispatcher
import jp.bucketeer.sdk.ext.getVariationValue
import jp.bucketeer.sdk.log.logd
import jp.bucketeer.sdk.log.loge
import jp.bucketeer.sdk.util.userOf
import org.json.JSONObject
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

interface FetchEvaluationsApiCallback {
  fun onSuccess(latencyMills: Long, sizeByte: Int, featureTag: String, state: String)
  fun onFailure(featureTag: String, e: StatusRuntimeException)
}

internal class BucketeerImpl constructor(
    context: Context,
    apiKey: String,
    endpoint: String,
    featureTag: String,
    config: BucketeerConfig,
    private val application: Application = context.applicationContext as Application,
    private val dataModule: DataModule = DataModule(application, apiKey, endpoint, featureTag),
    private val dispatcher: Dispatcher = Dispatcher(),
    private val actionModule: ActionCreatorModule = ActionCreatorModule(dispatcher, dataModule, featureTag),
    private val storeModule: StoreModule = StoreModule(dispatcher),
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(),
    private val clientInteractor: ClientInteractor = ClientInteractor(
        actionModule.clientInteractorActionCreator,
        actionModule.eventActionCreator,
        actionModule.latestEvaluationActionCreator,
        storeModule.latestEvaluationStore,
        storeModule.currentStore
    ),
    private val latestEvaluationUpdater: ScheduledTask = LatestEvaluationUpdater(
        config.pollingEvaluationIntervalMillis,
        actionModule.latestEvaluationActionCreator,
        clientInteractor.userHolder,
        executor
    ),
    private val eventSender: ScheduledTask = EventSender(
        config.logSendingIntervalMillis,
        config.logSendingMaxBatchQueueCount,
        actionModule.eventActionCreator,
        storeModule.eventStore,
        executor
    )
) : Bucketeer {

  init {
    try {
      // Here we set a callback function of get evaluation request. Since the
      // DataModule.ApiClient is instanciated before action creators, this call back
      // has to be set after actionModule is initialized. This is tricky codes but we
      // chose this instead of DI action creator to another action creator.
      // Hero is needed to rethink architecture!
      dataModule.api.setFetchEvaluationApiCallback(object : FetchEvaluationsApiCallback {
        override fun onSuccess(latencyMills: Long, sizeByte: Int, featureTag: String,
            state: String) {
          executor.execute {
            onHandleEvaluationApiSuccess(latencyMills, sizeByte, featureTag, state)
          }
        }

        override fun onFailure(featureTag: String, e: StatusRuntimeException) {
          executor.execute {
            onHandleEvaluationApiError(featureTag, e)
          }
        }
      })
    } catch (e: Exception) {
      actionModule.eventActionCreator.pushInternalErrorCountMetricsEvent(featureTag)
      loge(throwable = e)
    }
    logd { "Bucketeer initialized" }
  }

  override val isUserSet: Boolean get() = clientInteractor.isUserSet

  override fun setUser(userId: String) {
    setUser(userId, mapOf())
  }

  override fun setUser(
      userId: String,
      userData: Map<String, String>
  ) {
    logd {
      "Bucketeer.setUser(userId = $userId, userData = $userData) called"
    }
    val user = userOf(userId, userData)
    if (latestEvaluationUpdater.isStarted || eventSender.isStarted) {
      latestEvaluationUpdater.stop()
      eventSender.stop()

      clientInteractor.setUser(user)

      latestEvaluationUpdater.start()
      eventSender.start()
      return
    }
    clientInteractor.setUser(user)
  }

  override fun getUser(): User? {
    if (!clientInteractor.isUserSet) {
      return null
    }
    val user = clientInteractor.userHolder
    return User(
        user.user.id,
        user.user.dataMap
    )
  }

  override fun fetchUserEvaluations() {
    fetchUserEvaluations(null)
  }

  override fun fetchUserEvaluations(
      fetchUserEvaluationsCallback: Bucketeer.FetchUserEvaluationsCallback?
  ) {
    logd {
      "Bucketeer.fetchUserEvaluations(fetchUserEvaluationsCallback = $fetchUserEvaluationsCallback) called"
    }
    if (!isUserSet) {
      throw BucketeerException.IllegalStateException(
          "It is necessary to call setUser() before calling start()."
      )
    }
    executor.execute {
      clientInteractor.fetchUserEvaluations(fetchUserEvaluationsCallback)
    }
  }

  override fun getEvaluation(featureId: String): Evaluation? {
    val evaluation = clientInteractor.getLatestEvaluation(featureId)
    evaluation?.run {
      return Evaluation(
          id,
          featureId,
          featureVersion,
          userId,
          variationId,
          variationValue,
          reason.typeValue
      )
    } ?: return null
  }

  override fun getVariation(featureId: String, defaultValue: String): String {
    return getVariationStringInternal(featureId, defaultValue)
  }

  override fun getVariation(featureId: String, defaultValue: Int): Int {
    return getVariationStringInternal(featureId, defaultValue)
  }

  override fun getVariation(featureId: String, defaultValue: Long): Long {
    return getVariationStringInternal(featureId, defaultValue)
  }

  override fun getVariation(featureId: String, defaultValue: Float): Float {
    return getVariationStringInternal(featureId, defaultValue)
  }

  override fun getVariation(featureId: String, defaultValue: Double): Double {
    return getVariationStringInternal(featureId, defaultValue)
  }

  override fun getVariation(featureId: String, defaultValue: Boolean): Boolean {
    return getVariationStringInternal(featureId, defaultValue)
  }

  override fun getJsonVariation(featureId: String, defaultValue: JSONObject): JSONObject {
    return getVariationStringInternal(featureId, defaultValue)
  }

  private inline fun <reified T : Any> getVariationStringInternal(
      featureId: String,
      defaultValue: T
  ): T {
    logd { "Bucketeer.getVariation(featureId = $featureId, defaultValue = $defaultValue) called" }
    val evaluation = clientInteractor.getLatestEvaluation(featureId)
    val user = clientInteractor.userHolder.user
    if (evaluation != null) {
      executor.execute { clientInteractor.saveCurrentEvaluationEvent(user.id, evaluation) }
      executor.execute { clientInteractor.pushEvaluationEvent(user, evaluation) }
    } else {
      executor.execute { clientInteractor.pushDefaultEvaluationEvent(user, featureId) }
    }
    return evaluation.getVariationValue(defaultValue)
  }

  override fun track(goalId: String) {
    track(goalId, 0.0)
  }

  override fun track(goalId: String, value: Double) {
    val user = clientInteractor.userHolder.user
    executor.execute {
      clientInteractor.track(user, goalId, value)
    }
    logd { "Bucketeer.track(goalId = $goalId, value = $value) called" }
  }

  override fun start() {
    logd { "Bucketeer.start() called" }
    if (!isUserSet) {
      throw BucketeerException.IllegalStateException(
          "It is necessary to call setUser() before calling start()."
      )
    }
    executor.execute {
      latestEvaluationUpdater.start()
      eventSender.start()
    }
  }

  override fun stop() {
    logd { "Bucketeer.stop() called" }
    executor.execute {
      latestEvaluationUpdater.stop()
      eventSender.stop()
    }
  }

  private fun onHandleEvaluationApiSuccess(latencyMills: Long, sizeByte: Int, featureTag: String,
      state: String) {
    actionModule.eventActionCreator.pushGetEvaluationLatencyMetricsEvent(latencyMills,
        mapOf("tag" to featureTag, "state" to state))
    actionModule.eventActionCreator.pushGetEvaluationSizeMetricsEvent(sizeByte,
        mapOf("tag" to featureTag, "state" to state))
  }

  private fun onHandleEvaluationApiError(featureTag: String, e: StatusRuntimeException) {
    val action = actionModule.eventActionCreator
    when (e.status) {
      Status.DEADLINE_EXCEEDED -> action.pushTimeoutErrorCountMetricsEvent(featureTag)
      else -> action.pushInternalErrorCountMetricsEvent(featureTag)
    }
  }
}
