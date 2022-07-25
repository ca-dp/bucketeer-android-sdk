package jp.bucketeer.sdk

import org.json.JSONObject

interface BKTClientInterface {

  fun stringVariation(featureId: String, defaultValue: String): String

  fun intVariation(featureId: String, defaultValue: Int): Int

  fun doubleVariation(featureId: String, defaultValue: Double): Double

  fun booleanVariation(featureId: String, defaultValue: Boolean): Boolean

  fun jsonVariation(featureId: String, defaultValue: JSONObject): JSONObject

  fun track(goalId: String, value: Double = 0.0)

  fun currentUser(): BKTUser

  fun setUserAttributes(attributes: Map<String, String>)

  fun fetchEvaluations(
    fetchEvaluationsCallback: FetchEvaluationsCallback? = null
  )

  fun flush()

  fun evaluationDetails(featureId: String): BKTEvaluation?

  interface FetchEvaluationsCallback {
    fun onSuccess()
    fun onError(exception: BKTException)
  }
}
