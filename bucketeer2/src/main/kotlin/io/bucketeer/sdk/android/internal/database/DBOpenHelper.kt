package io.bucketeer.sdk.android.internal.database

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import io.bucketeer.sdk.android.internal.database.migration.Migration1to2
import io.bucketeer.sdk.android.internal.evaluation.db.CurrentEvaluationEntity
import io.bucketeer.sdk.android.internal.evaluation.db.LatestEvaluationEntity
import io.bucketeer.sdk.android.internal.event.EventEntity

class OpenHelperCallback : SupportSQLiteOpenHelper.Callback(VERSION) {

  companion object {
    const val FILE_NAME = "bucketeer.db"
    const val VERSION = 2
  }

  override fun onCreate(db: SupportSQLiteDatabase) {
    db.execSQL(
      """
      |CREATE TABLE ${CurrentEvaluationEntity.TABLE_NAME} (
      |   ${CurrentEvaluationEntity.COLUMN_USER_ID} TEXT NOT NULL,
      |   ${CurrentEvaluationEntity.COLUMN_FEATURE_ID} TEXT NOT NULL,
      |   ${CurrentEvaluationEntity.COLUMN_EVALUATION} TEXT NOT NULL,
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
      |   ${LatestEvaluationEntity.COLUMN_EVALUATION} TEXT,
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
      |   ${EventEntity.COLUMN_EVENT} TEXT
      |)
      """.trimMargin()
    )
  }

  override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
    if (oldVersion < 2) {
      Migration1to2().migrate(db)
    }
  }
}

fun createDatabase(
  context: Context,
  fileName: String? = OpenHelperCallback.FILE_NAME
): SupportSQLiteOpenHelper {
  val config = SupportSQLiteOpenHelper.Configuration.builder(context)
    .name(fileName)
    .callback(OpenHelperCallback())
    .build()

  return FrameworkSQLiteOpenHelperFactory().create(config)
}
