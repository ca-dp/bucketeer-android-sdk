package jp.bucketeer.sdk.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import jp.bucketeer.sdk.evaluation.db.CurrentEvaluationEntity
import jp.bucketeer.sdk.evaluation.db.LatestEvaluationEntity
import jp.bucketeer.sdk.events.EventEntity

internal class DatabaseOpenHelper(context: Context, fileName: String? = FILE_NAME) :
  SQLiteOpenHelper(context, fileName, null, VERSION) {

  companion object {
    const val FILE_NAME = "bucketeer.db"
    const val VERSION = 1
  }

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(
      """
      |CREATE TABLE ${CurrentEvaluationEntity.TABLE_NAME} (
      |   ${CurrentEvaluationEntity.COLUMN_USER_ID} TEXT NOT NULL,
      |   ${CurrentEvaluationEntity.COLUMN_FEATURE_ID} TEXT NOT NULL,
      |   ${CurrentEvaluationEntity.COLUMN_EVALUATION} BLOB NOT NULL,
      |   PRIMARY KEY(
      |     ${CurrentEvaluationEntity.COLUMN_USER_ID},
      |     ${CurrentEvaluationEntity.COLUMN_FEATURE_ID}
      |   )
      |)
      """.trimMargin()
    )

    db.execSQL(
      """
      |CREATE TABLE ${LatestEvaluationEntity.TABLE_NAME} (
      |   ${LatestEvaluationEntity.COLUMN_USER_ID} TEXT,
      |   ${LatestEvaluationEntity.COLUMN_FEATURE_ID} TEXT,
      |   ${LatestEvaluationEntity.COLUMN_EVALUATION} BLOB,
      |   PRIMARY KEY(
      |     ${LatestEvaluationEntity.COLUMN_USER_ID},
      |     ${LatestEvaluationEntity.COLUMN_FEATURE_ID}
      |   )
      |)
      """.trimMargin()
    )

    db.execSQL(
      """
      |CREATE TABLE ${EventEntity.TABLE_NAME} (
      |   ${EventEntity.COLUMN_ID} TEXT PRIMARY KEY,
      |   ${EventEntity.COLUMN_EVENT} BLOB
      |)
      """.trimMargin()
    )
  }

  override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
  }
}
