package jp.bucketeer.sdk.evaluation.db

import bucketeer.feature.EvaluationOuterClass
import jp.bucketeer.sdk.database.DatabaseOpenHelper
import jp.bucketeer.sdk.evaluation1
import jp.bucketeer.sdk.evaluation2
import jp.bucketeer.sdk.ext.getBlob
import jp.bucketeer.sdk.ext.getString
import jp.bucketeer.sdk.user1
import jp.bucketeer.sdk.user1Evaluations
import jp.bucketeer.sdk.user2
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class LatestEvaluationDaoImplTest {

  private lateinit var latestEvaluationDao: LatestEvaluationDaoImpl
  private lateinit var openHelper: DatabaseOpenHelper

  @Before
  fun setUp() {
    openHelper = DatabaseOpenHelper(RuntimeEnvironment.application, null)
    latestEvaluationDao = LatestEvaluationDaoImpl(openHelper)
  }

  @After
  fun tearDown() {
    openHelper.close()
  }

  @Test
  fun put_insertToDB() {
    latestEvaluationDao.put(user1, listOf(evaluation1))

    val projection = arrayOf(
      LatestEvaluationEntity.COLUMN_FEATURE_ID,
      LatestEvaluationEntity.COLUMN_USER_ID,
      LatestEvaluationEntity.COLUMN_EVALUATION,
    )

    val c = latestEvaluationDao.sqLiteOpenHelper.readableDatabase.query(
      LatestEvaluationEntity.TABLE_NAME,
      projection,
      null,
      null,
      null,
      null,
      null,
    )

    c.use {
      it.moveToFirst()
      assertEquals(
        evaluation1.featureId,
        c.getString(
          LatestEvaluationEntity.COLUMN_FEATURE_ID,
        ),
      )
      val blob = c.getBlob(LatestEvaluationEntity.COLUMN_EVALUATION)
      assertEquals(
        evaluation1,
        EvaluationOuterClass.Evaluation.newBuilder().mergeFrom(blob).build(),
      )

      assertEquals(c.moveToNext(), false)
    }
  }

  @Test
  fun get_returnEmptyIfAddNoItem() {
    val actual = latestEvaluationDao.get(user1)

    assertEquals(0, actual.size)
  }

  @Test
  fun get_returnSingleItemIfAddItem() {
    latestEvaluationDao.put(user1, user1Evaluations.evaluationsList)
    val actual = latestEvaluationDao.get(user1)

    assertEquals(2, actual.size)
    assertEquals(evaluation1, actual[0])
  }

  @Test
  fun get_returnMultipleItemIfAddItems() {
    latestEvaluationDao.put(user1, listOf(evaluation1))
    latestEvaluationDao.put(user1, listOf(evaluation2))

    val actual = latestEvaluationDao.get(user1)

    assertEquals(2, actual.size)
    assertEquals(evaluation1, actual[0])
    assertEquals(evaluation2, actual[1])
  }

  @Test
  fun deleteAllAndInsert_insert() {
    latestEvaluationDao.deleteAllAndInsert(user1, user1Evaluations.evaluationsList)

    latestEvaluationDao.get(user1) shouldBeEqualTo user1Evaluations.evaluationsList
  }

  @Test
  fun deleteAllAndInsert_deleteOld() {
    latestEvaluationDao.deleteAllAndInsert(user1, listOf(evaluation1))
    latestEvaluationDao.deleteAllAndInsert(user1, listOf(evaluation2))

    latestEvaluationDao.get(user1) shouldBeEqualTo listOf(evaluation2)
  }

  @Test
  fun deleteAllAndInsert_notDeleteIfUserChanged() {
    latestEvaluationDao.deleteAllAndInsert(user1, listOf(evaluation1))
    latestEvaluationDao.deleteAllAndInsert(user2, listOf(evaluation2))

    latestEvaluationDao.get(user1) shouldBeEqualTo listOf(evaluation1)
    latestEvaluationDao.get(user2) shouldBeEqualTo listOf(evaluation2)
  }
}
