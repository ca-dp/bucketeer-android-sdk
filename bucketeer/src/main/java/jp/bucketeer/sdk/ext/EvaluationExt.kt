package jp.bucketeer.sdk.ext

import bucketeer.feature.EvaluationOuterClass
import jp.bucketeer.sdk.log.logd
import org.json.JSONObject
import java.util.Locale

inline fun <reified T : Any> EvaluationOuterClass.Evaluation?.getVariationValue(
  defaultValue: T
): T {
  val value = this?.variationValue
  val typedValue: T = if (value != null) {
    @Suppress("IMPLICIT_CAST_TO_ANY")
    val anyValue = when (T::class) {
      String::class ->
        value
      Int::class -> value.toIntOrNull()
      Long::class -> value.toLongOrNull()
      Float::class -> value.toFloatOrNull()
      Double::class -> value.toDoubleOrNull()
      Boolean::class -> when (value.lowercase(Locale.ENGLISH)) {
        "true" -> true
        "false" -> false
        else -> null
      }
      JSONObject::class -> try {
        JSONObject(value)
      } catch (e: Exception) {
        null
      }
      else ->
        null
    }
    logd {
      if (anyValue == null) {
        "getVariation returns null reason: Cast fail"
      } else {
        null
      }
    }
    anyValue as? T ?: defaultValue
  } else {
    logd {
      "getVariation returns null reason: " + when {
        this == null -> {
          "Evaluation is null"
        }
        this.variationValue.isNullOrEmpty() -> {
          "Variation value is null or empty"
        }
        else -> {
          "Unknown"
        }
      }
    }
    defaultValue
  }
  return typedValue
}
