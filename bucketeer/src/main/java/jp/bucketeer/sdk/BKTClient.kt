package jp.bucketeer.sdk

import android.app.Application
import jp.bucketeer.sdk.util.SingletonHolder
import org.json.JSONObject

class BKTClient private constructor(
  private val application: Application,
  private val config: BKTConfig,
  private val user: BKTUser
) : BKTClientInterface {

  init {
    // TODO
  }

  companion object :
    SingletonHolder<BKTClientInterface, Application, BKTConfig, BKTUser>(::BKTClient)

  override fun stringVariation(featureId: String, defaultValue: String): String {
    TODO("Not yet implemented")
  }

  override fun intVariation(featureId: String, defaultValue: Int): Int {
    TODO("Not yet implemented")
  }

  override fun doubleVariation(featureId: String, defaultValue: Double): Double {
    TODO("Not yet implemented")
  }

  override fun booleanVariation(featureId: String, defaultValue: Boolean): Boolean {
    TODO("Not yet implemented")
  }

  override fun jsonVariation(featureId: String, defaultValue: JSONObject): JSONObject {
    TODO("Not yet implemented")
  }

  override fun track(goalId: String, value: Double) {
    TODO("Not yet implemented")
  }

  override fun currentUser(): BKTUser {
    return TODO("Not yet implemented")
  }

  override fun setUserAttributes(attributes: Map<String, String>) {
    TODO("Not yet implemented")
  }

  override fun fetchEvaluations(
    fetchUserEvaluationsCallback: BKTClientInterface.FetchEvaluationsCallback?
  ) {
    TODO("Not yet implemented")
  }

  override fun flush() {
    TODO("Not yet implemented")
  }

  override fun evaluationDetails(featureId: String): BKTEvaluation? {
    TODO("Not yet implemented")
  }
}
