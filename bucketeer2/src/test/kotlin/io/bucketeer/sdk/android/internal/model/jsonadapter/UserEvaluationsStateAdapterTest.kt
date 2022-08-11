package io.bucketeer.sdk.android.internal.model.jsonadapter

import com.google.common.truth.Truth.assertThat
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.squareup.moshi.JsonAdapter
import io.bucketeer.sdk.android.internal.di.DataModule
import io.bucketeer.sdk.android.internal.model.UserEvaluationsState
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class UserEvaluationsStateAdapterTest {

  lateinit var adapter: JsonAdapter<UserEvaluationsState>

  @Before
  fun setup() {
    adapter = DataModule.moshi().adapter(UserEvaluationsState::class.java)
  }

  @Test
  fun fromJson(@TestParameter state: UserEvaluationsState) {
    val result = adapter.fromJson(state.value.toString())

    assertThat(result).isEqualTo(state)
  }

  @Test
  fun toJson(@TestParameter state: UserEvaluationsState) {
    val result = adapter.toJson(state)

    assertThat(result).isEqualTo(state.value.toString())
  }
}
