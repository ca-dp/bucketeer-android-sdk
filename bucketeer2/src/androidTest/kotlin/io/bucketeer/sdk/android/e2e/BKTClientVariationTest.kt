package io.bucketeer.sdk.android.e2e

import android.content.Context
import androidx.test.annotation.UiThreadTest
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.bucketeer.sdk.android.BKTClient
import io.bucketeer.sdk.android.BKTConfig
import io.bucketeer.sdk.android.BKTUser
import io.bucketeer.sdk.android.BuildConfig
import io.bucketeer.sdk.android.internal.Constants
import io.bucketeer.sdk.android.internal.database.OpenHelperCallback
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BKTClientVariationTest {

  private lateinit var context: Context
  private lateinit var config: BKTConfig
  private lateinit var user: BKTUser

  @Before
  @UiThreadTest
  fun setup() {
    context = ApplicationProvider.getApplicationContext()

    config = BKTConfig.builder()
      .apiKey(BuildConfig.API_KEY)
      .endpoint(BuildConfig.API_URL)
      .featureTag(FEATURE_TAG)
      .build()

    user = BKTUser.builder()
      .id(USER_ID)
      .build()

    val result = BKTClient.initialize(context, config, user).get()

    assertThat(result).isNull()
  }

  @After
  @UiThreadTest
  fun tearDown() {
    BKTClient.destroy()
    context.deleteDatabase(OpenHelperCallback.FILE_NAME)
    context.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)
      .edit()
      .clear()
      .commit()
  }

  @Test
  fun stringVariation() {
    val result = BKTClient.getInstance()
      .stringVariation(FEATURE_ID_STRING, "test")
    assertThat(result).isEqualTo("value-1")
  }

  @Test
  fun intVariation() {
    val result = BKTClient.getInstance()
      .intVariation(FEATURE_ID_INT, 0)
    assertThat(result).isEqualTo(10)
  }

  @Test
  fun doubleVariation() {
    val result = BKTClient.getInstance()
      .doubleVariation(FEATURE_ID_DOUBLE, 0.1)
    assertThat(result).isEqualTo(2.1)
  }

  @Test
  fun booleanVariation() {
    val result = BKTClient.getInstance()
      .booleanVariation(FEATURE_ID_BOOLEAN, false)
    assertThat(result).isTrue()
  }

  @Test
  fun jsonVariation() {
    val result = BKTClient.getInstance()
      .jsonVariation(FEATURE_ID_JSON, JSONObject())

    val keys = result.keys().asSequence().toList()
    val values = keys.map { result.get(it) }
    assertThat(keys).isEqualTo(listOf("key"))
    assertThat(values).isEqualTo(listOf("value-1"))
  }
}
