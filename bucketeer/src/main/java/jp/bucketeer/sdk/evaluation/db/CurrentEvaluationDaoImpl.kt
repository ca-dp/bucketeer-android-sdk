package jp.bucketeer.sdk.evaluation.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import bucketeer.feature.EvaluationOuterClass
import jp.bucketeer.sdk.evaluation.db.CurrentEvaluationEntity.Companion.COLUMN_EVALUATION
import jp.bucketeer.sdk.evaluation.db.CurrentEvaluationEntity.Companion.COLUMN_FEATURE_ID
import jp.bucketeer.sdk.evaluation.db.CurrentEvaluationEntity.Companion.COLUMN_USER_ID
import jp.bucketeer.sdk.evaluation.db.CurrentEvaluationEntity.Companion.TABLE_NAME
import jp.bucketeer.sdk.ext.asSequence
import jp.bucketeer.sdk.ext.getBlob
import jp.bucketeer.sdk.ext.select
import jp.bucketeer.sdk.ext.transaction

internal class CurrentEvaluationDaoImpl(
  internal val sqLiteOpenHelper: SQLiteOpenHelper,
) : CurrentEvaluationDao {
  override fun upsertEvaluation(evaluation: EvaluationOuterClass.Evaluation) {
    val contentValues = ContentValues().apply {
      put(COLUMN_USER_ID, evaluation.userId)
      put(COLUMN_FEATURE_ID, evaluation.featureId)
      put(COLUMN_EVALUATION, evaluation.toByteArray())
    }

    sqLiteOpenHelper.writableDatabase.insertWithOnConflict(
      TABLE_NAME,
      null,
      contentValues,
      SQLiteDatabase.CONFLICT_REPLACE,
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
        whereArgs,
      )
    }
  }

  override fun getEvaluations(userId: String): List<EvaluationOuterClass.Evaluation> {
    val selection = "$COLUMN_USER_ID = ?"
    val selectionArgs = arrayOf(userId)
    val c = sqLiteOpenHelper.readableDatabase.select(
      table = TABLE_NAME,
      selection = selection,
      selectionArgs = selectionArgs,
    )

    return c.use {
      c.asSequence()
        .map {
          EvaluationOuterClass.Evaluation.newBuilder()
            .mergeFrom(it.getBlob(COLUMN_EVALUATION))
            .build()
        }
        .toList()
    }
  }
}
