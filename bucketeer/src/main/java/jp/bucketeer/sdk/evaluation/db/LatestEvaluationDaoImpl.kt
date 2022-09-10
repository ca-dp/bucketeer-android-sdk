package jp.bucketeer.sdk.evaluation.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import bucketeer.feature.EvaluationOuterClass
import bucketeer.user.UserOuterClass
import jp.bucketeer.sdk.evaluation.db.LatestEvaluationEntity.Companion.COLUMN_EVALUATION
import jp.bucketeer.sdk.evaluation.db.LatestEvaluationEntity.Companion.COLUMN_FEATURE_ID
import jp.bucketeer.sdk.evaluation.db.LatestEvaluationEntity.Companion.COLUMN_USER_ID
import jp.bucketeer.sdk.evaluation.db.LatestEvaluationEntity.Companion.TABLE_NAME
import jp.bucketeer.sdk.ext.asSequence
import jp.bucketeer.sdk.ext.getBlob
import jp.bucketeer.sdk.ext.transaction

internal class LatestEvaluationDaoImpl(
  val sqLiteOpenHelper: SQLiteOpenHelper,
) : LatestEvaluationDao {

  override fun put(user: UserOuterClass.User, list: List<EvaluationOuterClass.Evaluation>) {
    sqLiteOpenHelper.writableDatabase.transaction {
      list.forEach { evaluation ->
        val affectedRow = update(this@transaction, user, evaluation)
        if (affectedRow == 0) {
          insert(this@transaction, user, evaluation)
        }
      }
    }
  }

  private fun insert(
    database: SQLiteDatabase,
    user: UserOuterClass.User,
    evaluation: EvaluationOuterClass.Evaluation,
  ): Long {
    val contentValue = ContentValues().apply {
      put(COLUMN_USER_ID, user.id)
      put(COLUMN_FEATURE_ID, evaluation.featureId)
      put(COLUMN_EVALUATION, evaluation.toByteArray())
    }
    return database.insert(TABLE_NAME, null, contentValue)
  }

  private fun update(
    database: SQLiteDatabase,
    user: UserOuterClass.User,
    evaluation: EvaluationOuterClass.Evaluation,
  ): Int {
    val contentValues = ContentValues().apply {
      put(COLUMN_EVALUATION, evaluation.toByteArray())
    }
    return database.update(
      TABLE_NAME,
      contentValues,
      "$COLUMN_USER_ID=? AND $COLUMN_FEATURE_ID=?",
      arrayOf(user.id, evaluation.featureId),
    )
  }

  override fun get(user: UserOuterClass.User): List<EvaluationOuterClass.Evaluation> {
    val projection = arrayOf(COLUMN_USER_ID, COLUMN_EVALUATION)

    val c = sqLiteOpenHelper.readableDatabase.query(
      TABLE_NAME,
      projection,
      "$COLUMN_USER_ID=?",
      arrayOf(user.id),
      null,
      null,
      null,
    )

    return c.use {
      c.asSequence()
        .map {
          EvaluationOuterClass.Evaluation.parseFrom(
            it.getBlob(COLUMN_EVALUATION),
          )
        }.toList()
    }
  }

  private fun deleteAll(
    database: SQLiteDatabase,
    user: UserOuterClass.User,
  ) {
    database.delete(
      TABLE_NAME,
      "$COLUMN_USER_ID=?",
      arrayOf(user.id),
    )
  }

  override fun deleteAllAndInsert(
    user: UserOuterClass.User,
    list: List<EvaluationOuterClass.Evaluation>,
  ): Boolean {
    sqLiteOpenHelper.writableDatabase.transaction {
      deleteAll(this, user)
      list.forEach {
        if (insert(this, user, it) == -1L) {
          return false
        }
      }
    }
    return true
  }
}
