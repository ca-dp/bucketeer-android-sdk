package jp.bucketeer.sdk.evaluation.db

import androidx.test.core.app.ApplicationProvider
import bucketeer.feature.EvaluationOuterClass
import jp.bucketeer.sdk.database.DatabaseOpenHelper
import jp.bucketeer.sdk.evaluation1
import jp.bucketeer.sdk.evaluation2
import jp.bucketeer.sdk.ext.getBlob
import jp.bucketeer.sdk.ext.getString
import jp.bucketeer.sdk.user1Evaluations
import jp.bucketeer.sdk.user2Evaluations
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CurrentEvaluationDaoImplTest {
  private lateinit var currentEvaluationDao: CurrentEvaluationDaoImpl
  private lateinit var openHelper: DatabaseOpenHelper

  @Before
  fun setUp() {
    openHelper = DatabaseOpenHelper(ApplicationProvider.getApplicationContext(), null)
    currentEvaluationDao = CurrentEvaluationDaoImpl(openHelper)
  }

  @After
  fun tearDown() {
    openHelper.close()
  }

  @Test
  fun upsertEvaluation_insertToDB() {
    currentEvaluationDao.upsertEvaluation(user1Evaluations.evaluationsList[0])

    val projection = arrayOf(
      CurrentEvaluationEntity.COLUMN_USER_ID,
      CurrentEvaluationEntity.COLUMN_EVALUATION
    )

    val c = currentEvaluationDao.sqLiteOpenHelper.readableDatabase.query(
      CurrentEvaluationEntity.TABLE_NAME,
      projection,
      null,
      null,
      null,
      null,
      null
    )

    c.use {
      it.moveToFirst()
      c.getString(CurrentEvaluationEntity.COLUMN_USER_ID) shouldBeEqualTo "user id 1"
      val blob = c.getBlob(CurrentEvaluationEntity.COLUMN_EVALUATION)

      EvaluationOuterClass.Evaluation.newBuilder().mergeFrom(blob)
        .build() shouldBeEqualTo evaluation1
      c.moveToNext() shouldBeEqualTo false
    }
  }

  @Test
  fun upsertEvaluation_update() {
    val sourceEvaluation = user1Evaluations.evaluationsList[0]
    val variationValue = "update value"
    val updatedEvaluation = sourceEvaluation.toBuilder().setVariationValue(variationValue).build()

    currentEvaluationDao.upsertEvaluation(sourceEvaluation)
    val beforeEvaluations = currentEvaluationDao.getEvaluations("user id 1")
    beforeEvaluations[0].variationValue shouldBeEqualTo "test variation value1"

    currentEvaluationDao.upsertEvaluation(updatedEvaluation)
    val afterEvaluations = currentEvaluationDao.getEvaluations("user id 1")
    afterEvaluations[0].variationValue shouldBeEqualTo "update value"
  }

  @Test
  fun deleteNotIn_deleteAll() {
    currentEvaluationDao.upsertEvaluation(user1Evaluations.evaluationsList[0])
    currentEvaluationDao.upsertEvaluation(user1Evaluations.evaluationsList[1])
    currentEvaluationDao.upsertEvaluation(user2Evaluations.evaluationsList[0])

    currentEvaluationDao.deleteNotIn("user id 1", listOf("unknown id1", "unknown id2"))

    val evaluations1 = currentEvaluationDao.getEvaluations("user id 1")
    val evaluations2 = currentEvaluationDao.getEvaluations("user id 2")

    evaluations1.size shouldBeEqualTo 0

    evaluations2.size shouldBeEqualTo 1
    evaluations2[0].featureId shouldBeEqualTo "test-feature-3"
  }

  @Test
  fun deleteNotIn_deleteOneItem() {
    currentEvaluationDao.upsertEvaluation(user1Evaluations.evaluationsList[0])
    currentEvaluationDao.upsertEvaluation(user1Evaluations.evaluationsList[1])
    currentEvaluationDao.upsertEvaluation(user2Evaluations.evaluationsList[0])

    currentEvaluationDao.deleteNotIn("user id 1", listOf("test-feature-1", "unknown id1"))

    val evaluations1 = currentEvaluationDao.getEvaluations("user id 1")
    val evaluations2 = currentEvaluationDao.getEvaluations("user id 2")

    evaluations1.size shouldBeEqualTo 1
    evaluations1[0].featureId shouldBeEqualTo "test-feature-1"

    evaluations2.size shouldBeEqualTo 1
    evaluations2[0].featureId shouldBeEqualTo "test-feature-3"
  }

  @Test
  fun deleteNotIn_notDelete() {
    currentEvaluationDao.upsertEvaluation(user1Evaluations.evaluationsList[0])
    currentEvaluationDao.upsertEvaluation(user1Evaluations.evaluationsList[1])
    currentEvaluationDao.upsertEvaluation(user2Evaluations.evaluationsList[0])

    currentEvaluationDao.deleteNotIn("user id 1", listOf("test-feature-1", "test-feature-2"))

    val evaluations1 = currentEvaluationDao.getEvaluations("user id 1")
    val evaluations2 = currentEvaluationDao.getEvaluations("user id 2")

    evaluations1.size shouldBeEqualTo 2
    evaluations1[0].featureId shouldBeEqualTo "test-feature-1"
    evaluations1[1].featureId shouldBeEqualTo "test-feature-2"

    evaluations2.size shouldBeEqualTo 1
    evaluations2[0].featureId shouldBeEqualTo "test-feature-3"
  }

  @Test
  fun getEvaluations_returnEmptyIfAddNoItem() {
    val actual = currentEvaluationDao.getEvaluations("user id 1")

    actual.size shouldBeEqualTo 0
  }

  @Test
  fun getEvaluations_returnEmptyIfTargetUserItemIsEmpty() {
    currentEvaluationDao.upsertEvaluation(user2Evaluations.evaluationsList[0])
    val actual = currentEvaluationDao.getEvaluations("user id 1")

    actual.size shouldBeEqualTo 0
  }

  @Test
  fun getEvaluations_returnSingleItemIfAddTargetUserItem() {
    val evaluation = user1Evaluations.evaluationsList[0]

    currentEvaluationDao.upsertEvaluation(evaluation)
    val actual = currentEvaluationDao.getEvaluations("user id 1")

    actual.size shouldBeEqualTo 1
    actual[0] shouldBeEqualTo evaluation1
  }

  @Test
  fun getEvaluations_returnMultipleItemIfAddSomeItems() {
    currentEvaluationDao.upsertEvaluation(user1Evaluations.evaluationsList[0])
    currentEvaluationDao.upsertEvaluation(user2Evaluations.evaluationsList[0])
    currentEvaluationDao.upsertEvaluation(user1Evaluations.evaluationsList[1])

    val actual = currentEvaluationDao.getEvaluations("user id 1")

    actual.size shouldBeEqualTo 2
    actual[0] shouldBeEqualTo evaluation1
    actual[1] shouldBeEqualTo evaluation2
  }
}
