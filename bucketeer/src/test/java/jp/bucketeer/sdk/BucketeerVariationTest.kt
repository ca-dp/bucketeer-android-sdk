package jp.bucketeer.sdk

import bucketeer.feature.EvaluationOuterClass
import bucketeer.feature.VariationOuterClass
import jp.bucketeer.sdk.ext.getVariationValue
import org.amshove.kluent.shouldEqual
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class EvaluationExtTest(
    private val variationValue: String,
    private val defaultValue: Int,
    private val expectedValue: Int
) {
  companion object {
    @ParameterizedRobolectricTestRunner.Parameters(name = "getVariation Int: {0} -> {1}")
    @JvmStatic
    fun getTestData(): Collection<*> {
      return arrayOf(
          arrayOf("1", 0, 1),
          arrayOf("-1", 0, -1),
          arrayOf("1.0", 0, 0),
          arrayOf("1.0a", 0, 0),
          arrayOf("not int", 0, 0)
      ).toList()
    }
  }

  @Test
  fun getVariation_Int() {
    buildEvaluation(variationValue).getVariationValue(
        defaultValue
    ) shouldEqual expectedValue
  }

  @Test fun getVariation_Long() {
    buildEvaluation(variationValue).getVariationValue(
        defaultValue.toLong()
    ) shouldEqual expectedValue.toLong()
  }
}

@RunWith(ParameterizedRobolectricTestRunner::class)
class BucketeerVariationFloatTest(
    private val variationValue: String,
    private val defaultValue: Float,
    private val expectedValue: Float
) {
  companion object {
    @ParameterizedRobolectricTestRunner.Parameters(name = "getVariation Float: {0} -> {1}")
    @JvmStatic
    fun getTestData(): Collection<*> {
      return arrayOf(
          arrayOf("1", 0f, 1f),
          arrayOf("-1", 0f, -1f),
          arrayOf("1.0", 0f, 1.0f),
          arrayOf("not float", 0f, 0f)
      ).toList()
    }
  }

  @Test
  fun getVariation_Float() {
    buildEvaluation(variationValue).getVariationValue(
        defaultValue
    ) shouldEqual expectedValue
  }

  @Test fun getVariation_Double() {
    buildEvaluation(variationValue).getVariationValue(
        defaultValue.toDouble()
    ) shouldEqual expectedValue.toDouble()
  }
}

@RunWith(ParameterizedRobolectricTestRunner::class)
class BucketeerVariationStringTest(
    private val variationValue: String,
    private val defaultValue: String,
    private val expectedValue: String
) {
  companion object {
    @ParameterizedRobolectricTestRunner.Parameters(name = "getVariation String: {0} -> {1}")
    @JvmStatic
    fun getTestData(): Collection<*> {
      return arrayOf(
          arrayOf("1", "", "1"),
          arrayOf("-1", "", "-1"),
          arrayOf("1.0", "", "1.0"),
          arrayOf("string", "", "string"),
          arrayOf("true", "", "true"),
          arrayOf("false", "", "false"),
          arrayOf("""{}""", "", "{}")
      ).toList()
    }
  }

  @Test
  fun getVariation_String() {
    buildEvaluation(variationValue).getVariationValue(
        defaultValue
    ) shouldEqual expectedValue
  }
}

@RunWith(ParameterizedRobolectricTestRunner::class)
class BucketeerVariationBoolTest(
    private val variationValue: String,
    private val defaultValue: Boolean,
    private val expectedValue: Boolean
) {
  companion object {
    @ParameterizedRobolectricTestRunner.Parameters(
        name = """getVariation Boolean: ("{0}", {1}) -> {2}""")
    @JvmStatic
    fun getTestData(): Collection<*> {
      return arrayOf(
          arrayOf("true", false, true),
          arrayOf("false", true, false),
          arrayOf("true", true, true),
          arrayOf("TRUE", false, true),
          arrayOf("truea", false, false),
          arrayOf("not bool", false, false),
          arrayOf("not bool", true, true),
          arrayOf("1", false, false),
          arrayOf("1.0", false, false),
          arrayOf("{}", false, false)
      ).toList()
    }
  }

  @Test
  fun getVariation_Bool() {
    buildEvaluation(variationValue).getVariationValue(
        defaultValue
    ) shouldEqual expectedValue
  }
}

@RunWith(ParameterizedRobolectricTestRunner::class)
class BucketeerVariationJsonTest(
    private val variationValue: String,
    private val defaultValue: String,
    private val expectedValue: String
) {
  companion object {
    private const val JSON1 = """{ "key": "value"}"""

    @ParameterizedRobolectricTestRunner.Parameters(
        name = """getVariation Json: ("{0}", {1}) -> {2}""")
    @JvmStatic
    fun getTestData(): Collection<*> {
      return arrayOf(
          arrayOf(JSON1, "{}", JSON1),
          arrayOf("true", JSON1, JSON1),
          arrayOf("true", "{}", "{}"),
          arrayOf("not bool", "{}", "{}"),
          arrayOf("1", "{}", "{}"),
          arrayOf("1.0", "{}", "{}"),
          arrayOf("{}", "{}", "{}")
      ).toList()
    }
  }

  @Test fun getJsonVariation_JsonString() {
    val expected = JSONObject(expectedValue)
    val actual = buildEvaluation(variationValue).getVariationValue(
        JSONObject(defaultValue)
    )
    actual.keys().asSequence().toList() shouldEqual expected.keys().asSequence().toList()
    actual.keys().asSequence().toList().map {
      actual[it]
    } shouldEqual expected.keys().asSequence().toList().map {
      expected[it]
    }
  }
}

private fun buildEvaluation(value: String): EvaluationOuterClass.Evaluation? {
  return EvaluationOuterClass.Evaluation
      .newBuilder()
      .setFeatureId("feature_id")
      .setFeatureVersion(1)
      .setVariationId("variation_id")
      .setVariation(
          VariationOuterClass.Variation.newBuilder()
              .setId("variation_id")
              .setValue(value)
              .build())
      .build()
}
