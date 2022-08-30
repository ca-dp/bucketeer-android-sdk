package io.bucketeer.sdk.android.internal.evaluation.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.squareup.moshi.Moshi
import io.bucketeer.sdk.android.internal.database.asSequence
import io.bucketeer.sdk.android.internal.database.getString
import io.bucketeer.sdk.android.internal.database.select
import io.bucketeer.sdk.android.internal.database.transaction
import io.bucketeer.sdk.android.internal.evaluation.db.CurrentEvaluationEntity.COLUMN_EVALUATION
import io.bucketeer.sdk.android.internal.evaluation.db.CurrentEvaluationEntity.COLUMN_FEATURE_ID
import io.bucketeer.sdk.android.internal.evaluation.db.CurrentEvaluationEntity.COLUMN_USER_ID
import io.bucketeer.sdk.android.internal.evaluation.db.CurrentEvaluationEntity.TABLE_NAME
import io.bucketeer.sdk.android.internal.model.Evaluation

internal class CurrentEvaluationDaoImpl(
  private val sqLiteOpenHelper: SupportSQLiteOpenHelper,
  moshi: Moshi
) : CurrentEvaluationDao {

  private val adapter = moshi.adapter(Evaluation::class.java)

  override fun upsertEvaluation(evaluation: Evaluation) {
    val contentValues = ContentValues().apply {
      put(COLUMN_USER_ID, evaluation.user_id)
      put(COLUMN_FEATURE_ID, evaluation.feature_id)
      put(COLUMN_EVALUATION, adapter.toJson(evaluation))
    }
    sqLiteOpenHelper.writableDatabase.insert(
      TABLE_NAME,
      SQLiteDatabase.CONFLICT_REPLACE,
      contentValues
    )
  }

  override fun deleteNotIn(userId: String, featureIds: List<String>) {
    sqLiteOpenHelper.writableDatabase.transaction {
      val valuesNotIn = List(featureIds.count(), { "?" }).joinToString(separator = ",")
      val whereClause = "$COLUMN_USER_ID = ? AND " +
          "$COLUMN_FEATURE_ID NOT IN ($valuesNotIn)"
      val whereArgs = arrayOf(userId, *featureIds.toTypedArray())

      delete(
        TABLE_NAME,
        whereClause,
        whereArgs
      )
    }
  }

  override fun getEvaluations(userId: String): List<Evaluation> {
    val selection = "$COLUMN_USER_ID = ?"
    val selectionArgs = arrayOf(userId)
    val c = sqLiteOpenHelper.readableDatabase.select(
      table = TABLE_NAME,
      selection = selection,
      selectionArgs = selectionArgs
    )

    return c.use {
      c.asSequence()
        .mapNotNull {
          adapter.fromJson(it.getString(COLUMN_EVALUATION))
        }
        .toList()
    }
  }
}
