package io.bucketeer.sdk.android.internal.evaluation.db

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import io.bucketeer.sdk.android.internal.database.createDatabase
import io.bucketeer.sdk.android.internal.database.getString
import io.bucketeer.sdk.android.internal.database.select
import io.bucketeer.sdk.android.internal.di.DataModule
import io.bucketeer.sdk.android.internal.model.Evaluation
import io.bucketeer.sdk.android.mocks.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CurrentEvaluationDaoImplTest {

  private lateinit var dao: CurrentEvaluationDaoImpl
  private lateinit var openHelper: SupportSQLiteOpenHelper
  private lateinit var moshi: Moshi

  @Before
  fun setup() {
    moshi = DataModule.createMoshi()
    openHelper = createDatabase(ApplicationProvider.getApplicationContext())

    dao = CurrentEvaluationDaoImpl(openHelper, moshi)
  }

  @After
  fun tearDown() {
    openHelper.close()
  }

  @Test
  fun `upsertEvaluation - insert`() {
    dao.upsertEvaluation(user1Evaluations.evaluations[0])

    val c = getEvaluations()

    c.use {
      it.moveToFirst()

      val userId = it.getString(CurrentEvaluationEntity.COLUMN_USER_ID)
      assertThat(userId).isEqualTo("user id 1")

      val jsonStr = it.getString(CurrentEvaluationEntity.COLUMN_EVALUATION)
      val evaluation = moshi.adapter(Evaluation::class.java).fromJson(jsonStr)
      assertThat(evaluation).isEqualTo(evaluation1)

      assertThat(it.moveToNext()).isFalse()
    }
  }

  @Test
  fun `upsertEvaluation - update`() {
    val sourceEvaluation = user1Evaluations.evaluations[0]
    val variationValue = "updated value"
    val updatedEvaluation = sourceEvaluation.copy(
      variation_value = variationValue,
      variation = sourceEvaluation.variation.copy(value = variationValue)
    )

    dao.upsertEvaluation(sourceEvaluation)

    val beforeCursor = getEvaluations()
    beforeCursor.use {
      it.moveToFirst()

      val userId = it.getString(CurrentEvaluationEntity.COLUMN_USER_ID)
      assertThat(userId).isEqualTo("user id 1")

      val jsonStr = it.getString(CurrentEvaluationEntity.COLUMN_EVALUATION)
      val evaluation = moshi.adapter(Evaluation::class.java).fromJson(jsonStr)
      assertThat(evaluation).isEqualTo(sourceEvaluation)

      assertThat(it.moveToNext()).isFalse()
    }

    dao.upsertEvaluation(updatedEvaluation)

    val afterCursor = getEvaluations()
    afterCursor.use {
      it.moveToFirst()

      val userId = it.getString(CurrentEvaluationEntity.COLUMN_USER_ID)
      assertThat(userId).isEqualTo("user id 1")

      val jsonStr = it.getString(CurrentEvaluationEntity.COLUMN_EVALUATION)
      val evaluation = moshi.adapter(Evaluation::class.java).fromJson(jsonStr)
      assertThat(evaluation).isEqualTo(updatedEvaluation)

      assertThat(it.moveToNext()).isFalse()
    }
  }

  @Test
  fun `deleteNotIn - delete all`() {
    dao.upsertEvaluation(user1Evaluations.evaluations[0])
    dao.upsertEvaluation(user1Evaluations.evaluations[1])
    dao.upsertEvaluation(user2Evaluations.evaluations[0])

    dao.deleteNotIn("user id 1", listOf("unknown id1", "unknown id2"))

    val evaluation1 = dao.getEvaluations("user id 1")
    val evaluation2 = dao.getEvaluations("user id 2")

    assertThat(evaluation1).isEmpty()
    assertThat(evaluation2).hasSize(1)
    assertThat(evaluation2[0].feature_id).isEqualTo("test-feature-3")
  }

  @Test
  fun `deleteNotIn - delete 1 item`() {
    dao.upsertEvaluation(user1Evaluations.evaluations[0])
    dao.upsertEvaluation(user1Evaluations.evaluations[1])
    dao.upsertEvaluation(user2Evaluations.evaluations[0])

    dao.deleteNotIn("user id 1", listOf("test-feature-1", "unknown id1"))

    val evaluation1 = dao.getEvaluations("user id 1")
    val evaluation2 = dao.getEvaluations("user id 2")

    assertThat(evaluation1).hasSize(1)
    assertThat(evaluation1[0].feature_id).isEqualTo("test-feature-1")

    assertThat(evaluation2).hasSize(1)
    assertThat(evaluation2[0].feature_id).isEqualTo("test-feature-3")
  }

  @Test
  fun `deleteNotIn - delete none`() {
    dao.upsertEvaluation(user1Evaluations.evaluations[0])
    dao.upsertEvaluation(user1Evaluations.evaluations[1])
    dao.upsertEvaluation(user2Evaluations.evaluations[0])

    dao.deleteNotIn("user id 1", listOf("test-feature-1", "test-feature-2"))

    val evaluation1 = dao.getEvaluations("user id 1")
    val evaluation2 = dao.getEvaluations("user id 2")

    assertThat(evaluation1).hasSize(2)
    assertThat(evaluation1[0].feature_id).isEqualTo("test-feature-1")
    assertThat(evaluation1[1].feature_id).isEqualTo("test-feature-2")

    assertThat(evaluation2).hasSize(1)
    assertThat(evaluation2[0].feature_id).isEqualTo("test-feature-3")
  }

  @Test
  fun `getEvaluations - empty if no item`() {
    val actual = dao.getEvaluations("user id 1")

    assertThat(actual).isEmpty()
  }

  @Test
  fun `getEvaluations - empty if target user has no item`() {
    dao.upsertEvaluation(user2Evaluations.evaluations[0])

    val actual = dao.getEvaluations("user id 1")

    assertThat(actual).isEmpty()
  }

  @Test
  fun `getEvaluations - single item`() {
    dao.upsertEvaluation(evaluation1)

    val actual = dao.getEvaluations("user id 1")

    assertThat(actual).hasSize(1)
    assertThat(actual[0]).isEqualTo(evaluation1)
  }

  @Test
  fun `getEvaluations - multiple item`() {
    dao.upsertEvaluation(evaluation1)
    dao.upsertEvaluation(evaluation2)
    dao.upsertEvaluation(evaluation3)

    val actual = dao.getEvaluations("user id 1")

    assertThat(actual).hasSize(2)
    assertThat(actual[0]).isEqualTo(evaluation1)
    assertThat(actual[1]).isEqualTo(evaluation2)
  }

  private fun getEvaluations(): Cursor {
    val columns = arrayOf(
      CurrentEvaluationEntity.COLUMN_USER_ID,
      CurrentEvaluationEntity.COLUMN_EVALUATION
    )
    return openHelper.readableDatabase.select(
      table = CurrentEvaluationEntity.TABLE_NAME,
      columns = columns
    )
  }
}

