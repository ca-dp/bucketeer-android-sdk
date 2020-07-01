package jp.bucketeer.sdk.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.VisibleForTesting
import jp.bucketeer.sdk.Api
import jp.bucketeer.sdk.ApiClient
import jp.bucketeer.sdk.Constants
import jp.bucketeer.sdk.database.DatabaseOpenHelper
import jp.bucketeer.sdk.evaluation.db.CurrentEvaluationDao
import jp.bucketeer.sdk.evaluation.db.CurrentEvaluationDaoImpl
import jp.bucketeer.sdk.evaluation.db.LatestEvaluationDao
import jp.bucketeer.sdk.evaluation.db.LatestEvaluationDaoImpl
import jp.bucketeer.sdk.events.db.EventDao
import jp.bucketeer.sdk.events.db.EventDaoImpl

internal open class DataModule(
    application: Application,
    apiKey: String,
    endpoint: String,
    featureTag: String
) {
  @VisibleForTesting
  internal open val api: Api = ApiClient(apiKey, endpoint, featureTag)

  private val sqliteOpenHelper: SQLiteOpenHelper by lazy {
    DatabaseOpenHelper(application)
  }
  internal val currentEvaluationDao: CurrentEvaluationDao by lazy {
    CurrentEvaluationDaoImpl(sqliteOpenHelper)
  }
  internal val latestEvaluationDao: LatestEvaluationDao by lazy {
    LatestEvaluationDaoImpl(sqliteOpenHelper)
  }
  internal val eventDao: EventDao by lazy {
    EventDaoImpl(sqliteOpenHelper)
  }

  internal val sharedPreferences: SharedPreferences by lazy {
    application.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)
  }
}
