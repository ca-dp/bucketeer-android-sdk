package jp.bucketeer.sdk.test.integration

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import jp.bucketeer.sdk.Constants
import jp.bucketeer.sdk.dispatcher.Dispatcher
import jp.bucketeer.sdk.evaluation.LatestEvaluationActionCreator
import jp.bucketeer.sdk.userEvaluationsId1
import org.amshove.kluent.shouldBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
@MediumTest
class LatestEvaluationActionCreatorTest {

  private val sharedPref = getInstrumentation().targetContext.applicationContext
    .getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)

  @Before
  fun beforeExecute() {
    sharedPref.edit().clear().commit()
  }

  @Test
  fun initUserEvaluationsIdFromSharedPreferences() {
    var action = createLatestEvaluationActionCreator()
    action.updateUserEvaluationId(userEvaluationsId1)
    action = createLatestEvaluationActionCreator()
    action.currentUserEvaluationsId shouldBe userEvaluationsId1
  }

  @Test
  fun updateUserEvaluationsId() {
    val action = createLatestEvaluationActionCreator()
    action.currentUserEvaluationsId shouldBe ""
    action.updateUserEvaluationId(userEvaluationsId1)
    action.currentUserEvaluationsId shouldBe userEvaluationsId1
    sharedPref.getString(
      Constants.PREFERENCE_KEY_USER_EVALUATION_ID,
      "",
    ) shouldBe userEvaluationsId1
  }

  private fun createLatestEvaluationActionCreator(): LatestEvaluationActionCreator {
    return LatestEvaluationActionCreator(
      Dispatcher(),
      mock(),
      mock(),
      mock(),
      sharedPref,
    )
  }
}
