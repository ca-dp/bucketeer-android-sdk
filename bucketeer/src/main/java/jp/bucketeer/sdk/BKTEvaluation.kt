package jp.bucketeer.sdk

/**
 * Created by Alessandro Yuichi Okimoto on 2020-01-10.
 */
data class BKTEvaluation(
    var id: String,
    val featureId: String,
    val featureVersion: Int,
    val userId: String,
    val variationId: String,
    val variationValue: String,
    val reason: Int
)
