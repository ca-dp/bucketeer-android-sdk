package io.bucketeer.sdk.android.internal.di

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.squareup.moshi.Moshi
import io.bucketeer.sdk.android.BKTConfig
import io.bucketeer.sdk.android.internal.database.createDatabase
import io.bucketeer.sdk.android.internal.evaluation.db.CurrentEvaluationDao
import io.bucketeer.sdk.android.internal.evaluation.db.CurrentEvaluationDaoImpl
import io.bucketeer.sdk.android.internal.evaluation.db.LatestEvaluationDao
import io.bucketeer.sdk.android.internal.evaluation.db.LatestEvaluationDaoImpl
import io.bucketeer.sdk.android.internal.event.db.EventDao
import io.bucketeer.sdk.android.internal.event.db.EventDaoImpl
import io.bucketeer.sdk.android.internal.model.jsonadapter.EventAdapterFactory
import io.bucketeer.sdk.android.internal.model.jsonadapter.EventTypeAdapter
import io.bucketeer.sdk.android.internal.model.jsonadapter.MetricsEventAdapterFactory
import io.bucketeer.sdk.android.internal.model.jsonadapter.MetricsEventTypeAdapter
import io.bucketeer.sdk.android.internal.model.jsonadapter.ReasonTypeAdapter
import io.bucketeer.sdk.android.internal.model.jsonadapter.SourceIDAdapter
import io.bucketeer.sdk.android.internal.model.jsonadapter.UserEvaluationsStateAdapter
import io.bucketeer.sdk.android.internal.remote.ApiClient
import io.bucketeer.sdk.android.internal.remote.ApiClientImpl

internal class DataModule(
  application: Application,
  val config: BKTConfig
) {

  val moshi: Moshi by lazy { createMoshi() }

  val apiClient: ApiClient by lazy {
    ApiClientImpl(
      endpoint = config.endpoint,
      apiKey = config.apiKey,
      featureTag = config.featureTag,
      moshi = moshi
    )
  }

  private val sqliteOpenHelper: SupportSQLiteOpenHelper by lazy {
    createDatabase(application)
  }

  internal val currentEvaluationDao: CurrentEvaluationDao by lazy {
    CurrentEvaluationDaoImpl(sqliteOpenHelper, moshi)
  }

  internal val latestEvaluationDao: LatestEvaluationDao by lazy {
    LatestEvaluationDaoImpl(sqliteOpenHelper, moshi)
  }

  internal val eventDao: EventDao by lazy {
    EventDaoImpl(sqliteOpenHelper, moshi)
  }

  companion object {
    @VisibleForTesting
    internal fun createMoshi(): Moshi {
      return Moshi.Builder()
        .add(EventTypeAdapter())
        .add(MetricsEventTypeAdapter())
        .add(ReasonTypeAdapter())
        .add(SourceIDAdapter())
        .add(EventAdapterFactory())
        .add(MetricsEventAdapterFactory())
        .add(UserEvaluationsStateAdapter())
        .build()
    }
  }
}
